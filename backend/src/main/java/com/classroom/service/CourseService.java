package com.classroom.service;

import com.classroom.entity.Course;
import com.classroom.mapper.CourseMapper;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    private final CourseMapper courseMapper;
    private final JdbcTemplate jdbcTemplate;
    private final InputSecurityService inputSecurityService;
    private final BeanPropertyRowMapper<Course> rowMapper = BeanPropertyRowMapper.newInstance(Course.class);

    public CourseService(CourseMapper courseMapper, JdbcTemplate jdbcTemplate,
                         InputSecurityService inputSecurityService) {
        this.courseMapper = courseMapper;
        this.jdbcTemplate = jdbcTemplate;
        this.inputSecurityService = inputSecurityService;
    }

    public List<Course> list(String name, Long teacherId, Long studentId) {
        name = inputSecurityService.validateSearch(name, "course name");
        return jdbcTemplate.query("EXEC dbo.sp_course_list ?,?,?", rowMapper, name, teacherId, studentId);
    }

    public Course getById(Long id) {
        return courseMapper.selectById(id);
    }

    public boolean add(Course course) {
        return courseMapper.insert(course) > 0;
    }

    public boolean update(Course course) {
        return courseMapper.updateById(course) > 0;
    }

    public boolean delete(Long id) {
        return courseMapper.deleteById(id) > 0;
    }
}
