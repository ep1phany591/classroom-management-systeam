package com.classroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.classroom.entity.Classroom;
import com.classroom.entity.ClassroomBorrow;
import com.classroom.mapper.ClassroomBorrowMapper;
import com.classroom.mapper.CourseMapper;
import com.classroom.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class ClassroomBorrowService {

    private final ClassroomBorrowMapper borrowMapper;
    private final ClassroomService classroomService;
    private final TeacherMapper teacherMapper;
    private final CourseMapper courseMapper;
    private final InputSecurityService inputSecurityService;
    private final JdbcTemplate jdbcTemplate;
    private final BeanPropertyRowMapper<ClassroomBorrow> borrowRowMapper =
            BeanPropertyRowMapper.newInstance(ClassroomBorrow.class);

    public ClassroomBorrowService(ClassroomBorrowMapper borrowMapper, ClassroomService classroomService,
                                  TeacherMapper teacherMapper, CourseMapper courseMapper,
                                  InputSecurityService inputSecurityService, JdbcTemplate jdbcTemplate) {
        this.borrowMapper = borrowMapper;
        this.classroomService = classroomService;
        this.teacherMapper = teacherMapper;
        this.courseMapper = courseMapper;
        this.inputSecurityService = inputSecurityService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ClassroomBorrow> list(Long classroomId, Long teacherId, String date) {
        java.sql.Date sqlDate = null;
        if (date != null && !date.isEmpty()) {
            date = inputSecurityService.validateDate(date);
            sqlDate = java.sql.Date.valueOf(date);
        }
        return jdbcTemplate.query("EXEC dbo.sp_borrow_list ?,?,?", borrowRowMapper, classroomId, teacherId, sqlDate);
    }

    public boolean add(ClassroomBorrow borrow) {
        validateBorrow(borrow);
        return !jdbcTemplate.query("EXEC dbo.sp_borrow_create_transaction ?,?,?,?,?,?,?", borrowRowMapper,
                borrow.getClassroomId(), borrow.getTeacherId(), borrow.getCourseId(),
                java.sql.Date.valueOf(borrow.getBorrowDate()), borrow.getStartPeriod(),
                borrow.getEndPeriod(), borrow.getPurpose()).isEmpty();
    }

    public boolean cancel(Long id) {
        return !jdbcTemplate.query("EXEC dbo.sp_borrow_cancel_transaction ?", borrowRowMapper, id).isEmpty();
    }

    public boolean delete(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_borrow_delete_transaction ?", id);
        return true;
    }

    public ClassroomBorrow getById(Long id) {
        List<ClassroomBorrow> rows = jdbcTemplate.query("EXEC dbo.sp_borrow_get ?", borrowRowMapper, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private void validateBorrow(ClassroomBorrow borrow) {
        if (borrow == null || borrow.getClassroomId() == null || borrow.getTeacherId() == null
                || borrow.getBorrowDate() == null || borrow.getStartPeriod() == null || borrow.getEndPeriod() == null) {
            throw new IllegalArgumentException("教室、教师、日期和节次不能为空");
        }
        LocalDate borrowDate;
        try {
            borrowDate = LocalDate.parse(borrow.getBorrowDate());
        } catch (Exception exception) {
            throw new IllegalArgumentException("日期格式不正确");
        }
        if (borrowDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("不能借用过去日期的教室");
        }
        if (borrow.getStartPeriod() < 1 || borrow.getEndPeriod() > 10
                || borrow.getStartPeriod() > borrow.getEndPeriod()) {
            throw new IllegalArgumentException("节次范围必须在 1 到 10 之间，且开始节次不能晚于结束节次");
        }
        if (borrow.getPurpose() != null && borrow.getPurpose().length() > 200) {
            throw new IllegalArgumentException("用途不能超过 200 个字符");
        }
        Classroom classroom = classroomService.getById(borrow.getClassroomId());
        if (classroom == null || !Objects.equals(classroom.getRoomStatus(), 1)) {
            throw new IllegalArgumentException("教室不存在或当前不可借用");
        }
        if (teacherMapper.selectById(borrow.getTeacherId()) == null) {
            throw new IllegalArgumentException("教师不存在");
        }
        if (borrow.getCourseId() != null && courseMapper.selectById(borrow.getCourseId()) == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        borrow.setBorrowStatus(1);
    }

    /**
     * 每日教室空闲情况查询
     * 返回指定日期每个教室每个时段的占用情况
     */
    public List<Map<String, Object>> dailyFree(String date) {
        date = inputSecurityService.validateDate(date);
        List<Classroom> classrooms = classroomService.list(null, null);
        List<ClassroomBorrow> borrows = borrowMapper.selectList(
                new QueryWrapper<ClassroomBorrow>().eq("borrow_date", date).eq("borrow_status", 1));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Classroom room : classrooms) {
            Map<String, Object> item = new HashMap<>();
            item.put("classroomId", room.getId());
            item.put("classroomName", room.getName());
            item.put("building", room.getBuilding());
            item.put("capacity", room.getCapacity());
            item.put("type", room.getRoomType());

            boolean[] occupied = new boolean[11];
            for (ClassroomBorrow b : borrows) {
                if (b.getClassroomId().equals(room.getId())) {
                    for (int p = b.getStartPeriod(); p <= b.getEndPeriod(); p++) {
                        occupied[p] = true;
                    }
                }
            }
            List<String> freePeriods = new ArrayList<>();
            for (int p = 1; p <= 10; p++) {
                if (!occupied[p]) {
                    freePeriods.add("第" + p + "节");
                }
            }
            item.put("freePeriods", String.join(", ", freePeriods));
            item.put("allFree", freePeriods.size() == 10);
            result.add(item);
        }
        return result;
    }

    /**
     * 每周教室空闲情况表
     * 返回本周每个教室每天的占用统计
     */
    public List<Map<String, Object>> weeklyFree() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        List<Classroom> classrooms = classroomService.list(null, null);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> result = new ArrayList<>();
        for (Classroom room : classrooms) {
            Map<String, Object> item = new HashMap<>();
            item.put("classroomId", room.getId());
            item.put("classroomName", room.getName());
            item.put("building", room.getBuilding());
            item.put("capacity", room.getCapacity());

            Map<String, Integer> dayStatus = new LinkedHashMap<>();
            for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
                String ds = d.format(fmt);
                Long count = borrowMapper.selectCount(
                        new QueryWrapper<ClassroomBorrow>()
                                .eq("classroom_id", room.getId())
                                .eq("borrow_date", ds)
                                .eq("borrow_status", 1));
                dayStatus.put(ds, count.intValue());
            }
            item.put("dayBorrowCount", dayStatus);
            result.add(item);
        }
        return result;
    }

    /**
     * 每日教室上课情况表
     */
    public List<ClassroomBorrow> dailySchedule(String date) {
        date = inputSecurityService.validateDate(date);
        return borrowMapper.selectList(
                new QueryWrapper<ClassroomBorrow>()
                        .eq("borrow_date", date)
                        .eq("borrow_status", 1)
                        .orderByAsc("classroom_id")
                        .orderByAsc("start_period"));
    }

    /**
     * 每周教室上课情况表
     */
    public List<Map<String, Object>> weeklySchedule() {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<Map<String, Object>> result = new ArrayList<>();
        for (LocalDate d = monday; !d.isAfter(sunday); d = d.plusDays(1)) {
            String ds = d.format(fmt);
            Map<String, Object> dayItem = new HashMap<>();
            dayItem.put("date", ds);
            dayItem.put("dayOfWeek", d.getDayOfWeek().toString());
            List<ClassroomBorrow> list = borrowMapper.selectList(
                    new QueryWrapper<ClassroomBorrow>()
                            .eq("borrow_date", ds)
                            .eq("borrow_status", 1)
                            .orderByAsc("classroom_id")
                            .orderByAsc("start_period"));
            dayItem.put("borrows", list);
            result.add(dayItem);
        }
        return result;
    }
}
