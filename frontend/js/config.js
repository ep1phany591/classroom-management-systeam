const apiMap = {
    'admin-list': 'admin', 'teacher-list': 'teacher', 'student-list': 'student',
    'semester-list': 'semester', 'course-list': 'course', 'classroom-list': 'classroom',
    'teacher-courses': 'course', 'student-courses': 'course'
};

const columnConfig = {
    'admin-list': [
        ['id', 'ID', 60], ['username', '用户名'], ['name', '姓名'], ['phone', '电话'], ['email', '邮箱']
    ],
    'teacher-list': [
        ['id', 'ID', 60], ['username', '工号'], ['name', '姓名'], ['title', '职称'],
        ['department', '院系'], ['phone', '电话'], ['email', '邮箱']
    ],
    'student-list': [
        ['id', 'ID', 60], ['username', '学号'], ['name', '姓名'], ['className', '班级'],
        ['department', '院系'], ['phone', '电话'], ['email', '邮箱']
    ],
    'semester-list': [
        ['id', 'ID', 60], ['name', '学期名称'], ['startDate', '开始日期'], ['endDate', '结束日期'],
        ['isCurrent', '当前学期', 90]
    ],
    'course-list': [
        ['id', 'ID', 60], ['name', '课程名称'], ['code', '课程代码'], ['teacherId', '教师 ID', 80],
        ['semesterId', '学期 ID', 80], ['credit', '学分', 70], ['hours', '学时', 70], ['description', '描述']
    ],
    'teacher-courses': [
        ['id', 'ID', 60], ['name', '课程名称'], ['code', '课程代码'], ['semesterId', '学期 ID', 80],
        ['credit', '学分', 70], ['hours', '学时', 70], ['description', '描述']
    ],
    'student-courses': [
        ['id', 'ID', 60], ['name', '课程名称'], ['code', '课程代码'], ['teacherId', '教师 ID', 80],
        ['semesterId', '学期 ID', 80], ['credit', '学分', 70], ['hours', '学时', 70], ['description', '描述']
    ],
    'classroom-list': [
        ['id', 'ID', 60], ['name', '教室名称'], ['building', '教学楼'], ['floorNum', '楼层', 60],
        ['capacity', '容量', 70], ['roomType', '类型'], ['hasProjector', '投影', 60],
        ['hasAc', '空调', 60], ['roomStatus', '状态', 70], ['description', '描述']
    ]
};
Object.keys(columnConfig).forEach(key => {
    columnConfig[key] = columnConfig[key].map(([prop, label, width]) => ({ prop, label, width }));
});

const dialogFieldsConfig = {
    'admin-list': [['username','用户名'],['password','密码'],['name','姓名'],['phone','电话'],['email','邮箱']],
    'teacher-list': [['username','工号'],['password','密码'],['name','姓名'],['title','职称'],['department','院系'],['phone','电话'],['email','邮箱']],
    'student-list': [['username','学号'],['password','密码'],['name','姓名'],['className','班级'],['department','院系'],['phone','电话'],['email','邮箱']],
    'semester-list': [['name','学期名称'],['startDate','开始日期','date'],['endDate','结束日期','date'],['isCurrent','当前学期','switch']],
    'course-list': [['name','课程名称'],['code','课程代码'],['teacherId','教师 ID','number'],['semesterId','学期 ID','number'],['credit','学分','number'],['hours','学时','number'],['description','描述']],
    'classroom-list': [['name','教室名称'],['building','教学楼'],['floorNum','楼层','number'],['capacity','容量','number'],['roomType','类型'],['hasProjector','有投影','switch'],['hasAc','有空调','switch'],['roomStatus','状态','number'],['description','描述']]
};
Object.keys(dialogFieldsConfig).forEach(key => {
    dialogFieldsConfig[key] = dialogFieldsConfig[key].map(([prop, label, type = 'input']) => ({ prop, label, type }));
});
