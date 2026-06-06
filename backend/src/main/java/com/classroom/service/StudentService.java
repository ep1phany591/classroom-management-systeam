package com.classroom.service;

import com.classroom.entity.Student;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentService {
    private final JdbcTemplate jdbcTemplate;
    private final PasswordService passwordService;
    private final InputSecurityService inputSecurityService;
    private final BeanPropertyRowMapper<Student> rowMapper = BeanPropertyRowMapper.newInstance(Student.class);

    public StudentService(JdbcTemplate jdbcTemplate, PasswordService passwordService,
                          InputSecurityService inputSecurityService) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordService = passwordService;
        this.inputSecurityService = inputSecurityService;
    }

    public List<Student> list(String name) {
        name = inputSecurityService.validateSearch(name, "student name");
        return jdbcTemplate.query("EXEC dbo.sp_student_list ?", rowMapper, name);
    }

    public Student getById(Long id) {
        List<Student> rows = jdbcTemplate.query("EXEC dbo.sp_student_get ?", rowMapper, id);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public boolean add(Student student) {
        student.setPassword(passwordService.hashIfPresent(student.getPassword()));
        return !jdbcTemplate.query("EXEC dbo.sp_student_create ?,?,?,?,?,?,?", rowMapper,
                student.getUsername(), student.getPassword(), student.getName(), student.getClassName(),
                student.getDepartment(), student.getPhone(), student.getEmail()).isEmpty();
    }

    public boolean update(Student student) {
        student.setPassword(passwordService.hashIfPresent(student.getPassword()));
        return !jdbcTemplate.query("EXEC dbo.sp_student_update ?,?,?,?,?,?,?,?", rowMapper,
                student.getId(), student.getUsername(), student.getPassword(), student.getName(),
                student.getClassName(), student.getDepartment(), student.getPhone(), student.getEmail()).isEmpty();
    }

    public boolean delete(Long id) {
        jdbcTemplate.update("EXEC dbo.sp_student_delete ?", id);
        return true;
    }
}
