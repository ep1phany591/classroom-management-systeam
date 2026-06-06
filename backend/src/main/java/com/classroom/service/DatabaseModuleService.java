package com.classroom.service;

import com.classroom.entity.Classroom;
import com.classroom.entity.ClassroomBorrow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.sql.Date;
import java.util.List;
import java.util.Map;

@Service
public class DatabaseModuleService {

    private final JdbcTemplate jdbcTemplate;
    private final InputSecurityService inputSecurityService;

    public DatabaseModuleService(JdbcTemplate jdbcTemplate, InputSecurityService inputSecurityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.inputSecurityService = inputSecurityService;
    }

    public List<Map<String, Object>> listClassrooms(String name) {
        name = inputSecurityService.validateSearch(name, "教室名称");
        return jdbcTemplate.queryForList("EXEC dbo.sp_classroom_list ?,?", name, null);
    }

    public List<Map<String, Object>> getClassroom(Long id) {
        return jdbcTemplate.queryForList("EXEC dbo.sp_classroom_get ?", requiredId(id));
    }

    public List<Map<String, Object>> createClassroom(Classroom classroom) {
        validateClassroom(classroom, false);
        return jdbcTemplate.queryForList("EXEC dbo.sp_classroom_create ?,?,?,?,?,?,?,?,?",
                classroom.getName(), classroom.getBuilding(), classroom.getFloorNum(), classroom.getCapacity(),
                classroom.getRoomType(), defaultValue(classroom.getHasProjector(), 0),
                defaultValue(classroom.getHasAc(), 0), defaultValue(classroom.getRoomStatus(), 1),
                classroom.getDescription());
    }

    public List<Map<String, Object>> updateClassroom(Classroom classroom) {
        validateClassroom(classroom, true);
        return jdbcTemplate.queryForList("EXEC dbo.sp_classroom_update ?,?,?,?,?,?,?,?,?,?",
                classroom.getId(), classroom.getName(), classroom.getBuilding(), classroom.getFloorNum(),
                classroom.getCapacity(), classroom.getRoomType(), defaultValue(classroom.getHasProjector(), 0),
                defaultValue(classroom.getHasAc(), 0), defaultValue(classroom.getRoomStatus(), 1),
                classroom.getDescription());
    }

    public void deleteClassroom(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_classroom_delete ?", requiredId(id));
    }

    public List<Map<String, Object>> createBorrowTransaction(ClassroomBorrow borrow) {
        if (borrow == null || borrow.getClassroomId() == null || borrow.getTeacherId() == null
                || borrow.getStartPeriod() == null || borrow.getEndPeriod() == null) {
            throw new IllegalArgumentException("教室、教师和节次不能为空");
        }
        String date = inputSecurityService.validateDate(borrow.getBorrowDate());
        if (borrow.getPurpose() != null && borrow.getPurpose().length() > 200) {
            throw new IllegalArgumentException("用途不能超过 200 个字符");
        }
        return jdbcTemplate.queryForList("EXEC dbo.sp_borrow_create_transaction ?,?,?,?,?,?,?",
                borrow.getClassroomId(), borrow.getTeacherId(), borrow.getCourseId(), Date.valueOf(date),
                borrow.getStartPeriod(), borrow.getEndPeriod(), borrow.getPurpose());
    }

    public List<Map<String, Object>> cancelBorrowTransaction(Long id) {
        return jdbcTemplate.queryForList("EXEC dbo.sp_borrow_cancel_transaction ?", requiredId(id));
    }

    public void deleteBorrowTransaction(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_borrow_delete_transaction ?", requiredId(id));
    }

    public List<Map<String, Object>> listAuditLogs(Integer limit) {
        int rowLimit = limit == null ? 50 : Math.max(1, Math.min(limit, 200));
        return jdbcTemplate.queryForList(
                "SELECT TOP (?) id, table_name, operation_type, record_id, detail, created_at "
                        + "FROM dbo.database_audit_log ORDER BY id DESC", rowLimit);
    }

    public List<Map<String, Object>> analyzeUsage(String startDate, String endDate) {
        Date start = toOptionalDate(startDate);
        Date end = toOptionalDate(endDate);
        if (start != null && end != null && start.after(end)) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        return jdbcTemplate.queryForList("EXEC dbo.sp_classroom_usage_analysis ?,?", start, end);
    }

    public List<Map<String, Object>> readUsageView() {
        return jdbcTemplate.queryForList("SELECT * FROM dbo.vw_classroom_usage_analysis ORDER BY occupied_period_count DESC");
    }

    private void validateClassroom(Classroom classroom, boolean requireId) {
        if (classroom == null || !StringUtils.hasText(classroom.getName())) {
            throw new IllegalArgumentException("教室名称不能为空");
        }
        if (requireId) {
            requiredId(classroom.getId());
        }
        inputSecurityService.validateSearch(classroom.getName(), "教室名称");
        inputSecurityService.validateSearch(classroom.getBuilding(), "教学楼");
        if (classroom.getCapacity() != null && classroom.getCapacity() < 0) {
            throw new IllegalArgumentException("容量不能为负数");
        }
    }

    private Date toOptionalDate(String value) {
        return StringUtils.hasText(value) ? Date.valueOf(inputSecurityService.validateDate(value)) : null;
    }

    private Long requiredId(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID 必须为正整数");
        }
        return id;
    }

    private Integer defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
