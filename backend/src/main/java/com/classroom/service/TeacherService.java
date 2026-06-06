package com.classroom.service;

import com.classroom.entity.Teacher;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordService passwordService;
    private final InputSecurityService inputSecurityService;
    private final BeanPropertyRowMapper<Teacher> rowMapper = BeanPropertyRowMapper.newInstance(Teacher.class);

    public TeacherService(JdbcTemplate jdbcTemplate, PasswordService passwordService,
                          InputSecurityService inputSecurityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordService = passwordService;
        this.inputSecurityService = inputSecurityService;
    }

    public List<Teacher> list(String name) {
        name = inputSecurityService.validateSearch(name, "teacher name");
        return jdbcTemplate.query("EXEC dbo.sp_teacher_list ?", rowMapper, name);
    }

    public Teacher getById(Long id) {
        List<Teacher> rows = jdbcTemplate.query("EXEC dbo.sp_teacher_get ?", rowMapper, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean add(Teacher teacher) {
        teacher.setPassword(passwordService.hashIfPresent(teacher.getPassword()));
        return !jdbcTemplate.query("EXEC dbo.sp_teacher_create ?,?,?,?,?,?,?", rowMapper,
                teacher.getUsername(), teacher.getPassword(), teacher.getName(), teacher.getTitle(),
                teacher.getDepartment(), teacher.getPhone(), teacher.getEmail()).isEmpty();
    }

    public boolean update(Teacher teacher) {
        teacher.setPassword(passwordService.hashIfPresent(teacher.getPassword()));
        return !jdbcTemplate.query("EXEC dbo.sp_teacher_update ?,?,?,?,?,?,?,?", rowMapper,
                teacher.getId(), teacher.getUsername(), teacher.getPassword(), teacher.getName(),
                teacher.getTitle(), teacher.getDepartment(), teacher.getPhone(), teacher.getEmail()).isEmpty();
    }

    public boolean delete(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_teacher_delete ?", id);
        return true;
    }
}
