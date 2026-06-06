const { createApp, ref, computed, reactive, watch } = Vue;
const { ElMessage, ElMessageBox } = ElementPlus;

createApp({
    setup() {
        const savedUser = localStorage.getItem('classroom_user');
        const user = ref(savedUser ? JSON.parse(savedUser) : null);
        const loginForm = reactive({ username: 'admin', password: '123456', role: 'admin' });
        const page = ref(user.value ? homePage(user.value.role) : 'admin-dashboard');
        const tableData = ref([]);
        const tableLoading = ref(false);
        const searchName = ref('');
        const stats = ref([]);
        const dialogVisible = ref(false);
        const dialogForm = reactive({});
        const currentEditId = ref(null);
        const borrowDate = ref('');
        const borrowForm = reactive(defaultBorrow());
        const classroomList = ref([]);
        const teacherList = ref([]);
        const courseList = ref([]);
        const borrowDialogVisible = ref(false);
        const freeDate = ref('');
        const freeTableData = ref([]);
        const weeklyFreeData = ref([]);
        const scheduleDate = ref('');
        const scheduleTableData = ref([]);
        const weeklyScheduleData = ref([]);

        const roleName = computed(() => ({ admin: '管理员', teacher: '教师', student: '学生' })[user.value?.role] || '');
        const currentColumns = computed(() => columnConfig[page.value] || []);
        const dialogFields = computed(() => dialogFieldsConfig[page.value] || []);
        const canEdit = computed(() => user.value?.role === 'admin' && dialogFields.value.length > 0);

        async function doLogin() {
            try {
                const res = await API.post('/auth/login', { ...loginForm });
                if (res.data.code !== 200) return ElMessage.error(res.data.message);
                user.value = res.data.data;
                localStorage.setItem('classroom_token', user.value.token);
                localStorage.setItem('classroom_user', JSON.stringify(user.value));
                page.value = homePage(user.value.role);
                loadCurrentPage();
            } catch (error) { messageError(error, '登录失败'); }
        }
        async function logout() {
            try { await API.post('/auth/logout'); } catch {}
            localStorage.removeItem('classroom_token');
            localStorage.removeItem('classroom_user');
            user.value = null;
        }
        function navigate(nextPage) { page.value = nextPage; }
        function homePage(role) { return role === 'admin' ? 'admin-dashboard' : role === 'teacher' ? 'teacher-dashboard' : 'student-dashboard'; }

        async function loadStats() {
            try {
                const [teachers, students, courses, classrooms] = await Promise.all([
                    API.get('/teacher/list'), API.get('/student/list'), API.get('/course/list'), API.get('/classroom/list')
                ]);
                stats.value = [
                    ['教师', teachers], ['学生', students], ['课程', courses], ['教室', classrooms]
                ].map(([label, res]) => ({ label, value: res.data.data?.length || 0 }));
            } catch (error) { messageError(error, '统计加载失败'); }
        }
        async function loadTable() {
            const api = apiMap[page.value];
            if (!api) return;
            tableLoading.value = true;
            try {
                const params = {};
                if (page.value === 'teacher-courses') params.teacherId = user.value.id;
                if (page.value === 'student-courses') params.studentId = user.value.id;
                if (searchName.value) params.name = searchName.value;
                tableData.value = (await API.get(`/${api}/list`, { params })).data.data || [];
            } catch (error) { messageError(error, '列表加载失败'); }
            finally { tableLoading.value = false; }
        }
        function openDialog(row) {
            Object.keys(dialogForm).forEach(key => delete dialogForm[key]);
            if (row) Object.assign(dialogForm, row);
            currentEditId.value = row?.id || null;
            dialogVisible.value = true;
        }
        async function doSave() {
            const api = apiMap[page.value];
            try {
                const payload = { ...dialogForm };
                if (currentEditId.value) payload.id = currentEditId.value;
                const res = currentEditId.value ? await API.put(`/${api}`, payload) : await API.post(`/${api}`, payload);
                if (res.data.code !== 200) return ElMessage.error(res.data.message);
                ElMessage.success(res.data.message);
                dialogVisible.value = false;
                loadTable();
            } catch (error) { messageError(error, '保存失败'); }
        }
        async function doDelete(id) {
            try {
                await ElMessageBox.confirm('确定删除此记录吗？', '确认', { type: 'warning' });
                const api = page.value === 'borrow-list' ? 'borrow' : apiMap[page.value];
                const res = await API.delete(`/${api}/${id}`);
                if (res.data.code !== 200) return ElMessage.error(res.data.message);
                ElMessage.success('删除成功');
                page.value === 'borrow-list' ? loadBorrows() : loadTable();
            } catch (error) { if (error !== 'cancel') messageError(error, '删除失败'); }
        }

        async function loadBorrowOptions() {
            const requests = [API.get('/classroom/list'), API.get('/course/list')];
            if (user.value.role === 'admin') requests.push(API.get('/teacher/list'));
            const [rooms, courses, teachers] = await Promise.all(requests);
            classroomList.value = rooms.data.data || [];
            courseList.value = courses.data.data || [];
            teacherList.value = teachers?.data.data || [];
        }
        async function loadBorrows() {
            try {
                const params = {};
                if (borrowDate.value) params.date = borrowDate.value;
                if (page.value === 'teacher-borrows') params.teacherId = user.value.id;
                tableData.value = (await API.get('/borrow/list', { params })).data.data || [];
            } catch (error) { messageError(error, '借用记录加载失败'); }
        }
        function openBorrowDialog() {
            Object.assign(borrowForm, defaultBorrow());
            borrowDialogVisible.value = true;
            loadBorrowOptions();
        }
        async function doBorrow() {
            try {
                const payload = { ...borrowForm };
                if (user.value.role === 'teacher') payload.teacherId = user.value.id;
                const res = await API.post('/borrow', payload);
                if (res.data.code !== 200) return ElMessage.error(res.data.message);
                ElMessage.success(res.data.message);
                borrowDialogVisible.value = false;
                if (page.value === 'borrow-list' || page.value === 'teacher-borrows') loadBorrows();
            } catch (error) { messageError(error, '借用失败'); }
        }
        async function cancelBorrow(id) {
            try {
                await ElMessageBox.confirm('确定取消此借用吗？', '确认', { type: 'warning' });
                const res = await API.put(`/borrow/cancel/${id}`);
                if (res.data.code !== 200) return ElMessage.error(res.data.message);
                ElMessage.success('取消成功');
                loadBorrows();
            } catch (error) { if (error !== 'cancel') messageError(error, '取消失败'); }
        }
        async function loadDailyFree() { await loadQuery('/borrow/daily-free', { date: freeDate.value }, freeTableData, '请选择日期'); }
        async function loadWeeklyFree() { await loadQuery('/borrow/weekly-free', {}, weeklyFreeData); }
        async function loadDailySchedule() { await loadQuery('/borrow/daily-schedule', { date: scheduleDate.value }, scheduleTableData, '请选择日期'); }
        async function loadWeeklySchedule() { await loadQuery('/borrow/weekly-schedule', {}, weeklyScheduleData); }
        async function loadQuery(url, params, target, emptyMessage) {
            if (emptyMessage && !params.date) return ElMessage.warning(emptyMessage);
            try { target.value = (await API.get(url, { params })).data.data || []; }
            catch (error) { messageError(error, '查询失败'); }
        }
        function loadCurrentPage() {
            searchName.value = '';
            if (page.value === 'admin-dashboard') loadStats();
            else if (apiMap[page.value]) loadTable();
            else if (page.value === 'borrow-list' || page.value === 'teacher-borrows') loadBorrows();
            else if (page.value === 'borrow-add') loadBorrowOptions();
        }
        function messageError(error, fallback) { ElMessage.error(error.response?.data?.message || fallback); }
        function defaultBorrow() { return { classroomId: null, teacherId: null, courseId: null, borrowDate: '', startPeriod: 1, endPeriod: 2, purpose: '' }; }
        watch(page, loadCurrentPage);
        if (user.value) loadCurrentPage();

        return { user, loginForm, page, stats, tableData, tableLoading, searchName, roleName, currentColumns, dialogFields, canEdit,
            dialogVisible, dialogForm, currentEditId, doLogin, logout, navigate, loadTable, openDialog, doSave, doDelete,
            borrowDate, borrowForm, classroomList, teacherList, courseList, borrowDialogVisible, openBorrowDialog, doBorrow, loadBorrows, cancelBorrow,
            freeDate, freeTableData, weeklyFreeData, loadDailyFree, loadWeeklyFree,
            scheduleDate, scheduleTableData, weeklyScheduleData, loadDailySchedule, loadWeeklySchedule };
    }
}).use(ElementPlus).mount('#app');
