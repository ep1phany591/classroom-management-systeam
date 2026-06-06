package com.classroom.service;

import com.classroom.entity.Classroom;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ClassroomService {
    private final JdbcTemplate jdbcTemplate;
    private final InputSecurityService inputSecurityService;
    private final BeanPropertyRowMapper<Classroom> rowMapper = BeanPropertyRowMapper.newInstance(Classroom.class);

    public ClassroomService(JdbcTemplate jdbcTemplate, InputSecurityService inputSecurityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.inputSecurityService = inputSecurityService;
    }

    public List<Classroom> list(String name, String building) {
        name = inputSecurityService.validateSearch(name, "classroom name");
        building = inputSecurityService.validateSearch(building, "building");
        return jdbcTemplate.query("EXEC dbo.sp_classroom_list ?,?", rowMapper, name, building);
    }

    public Classroom getById(Long id) {
        List<Classroom> rows = jdbcTemplate.query("EXEC dbo.sp_classroom_get ?", rowMapper, requiredId(id));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean add(Classroom classroom) {
        validate(classroom, false);
        return !jdbcTemplate.query("EXEC dbo.sp_classroom_create ?,?,?,?,?,?,?,?,?", rowMapper,
                classroom.getName(), classroom.getBuilding(), classroom.getFloorNum(), classroom.getCapacity(),
                classroom.getRoomType(), defaultValue(classroom.getHasProjector(), 0),
                defaultValue(classroom.getHasAc(), 0), defaultValue(classroom.getRoomStatus(), 1),
                classroom.getDescription()).isEmpty();
    }

    public boolean update(Classroom classroom) {
        validate(classroom, true);
        return !jdbcTemplate.query("EXEC dbo.sp_classroom_update ?,?,?,?,?,?,?,?,?,?", rowMapper,
                classroom.getId(), classroom.getName(), classroom.getBuilding(), classroom.getFloorNum(),
                classroom.getCapacity(), classroom.getRoomType(), defaultValue(classroom.getHasProjector(), 0),
                defaultValue(classroom.getHasAc(), 0), defaultValue(classroom.getRoomStatus(), 1),
                classroom.getDescription()).isEmpty();
    }

    public boolean delete(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_classroom_delete ?", requiredId(id));
        return true;
    }

    private void validate(Classroom classroom, boolean requireId) {
        if (classroom == null || !StringUtils.hasText(classroom.getName())) {
            throw new IllegalArgumentException("classroom name is required");
        }
        if (requireId) requiredId(classroom.getId());
        inputSecurityService.validateSearch(classroom.getName(), "classroom name");
        inputSecurityService.validateSearch(classroom.getBuilding(), "building");
        if (classroom.getCapacity() != null && classroom.getCapacity() < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
    }

    private Long requiredId(Long id) {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
        return id;
    }

    private Integer defaultValue(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }
}
