package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Course;
import com.classroom.service.CourseService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping("/list")
    public Result<List<Course>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId) {
        return Result.success(courseService.list(name, teacherId, studentId));
    }

    @GetMapping("/{id}")
    public Result<Course> getById(@PathVariable Long id) {
        return Result.success(courseService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Course course) {
        if (courseService.add(course)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Course course) {
        if (courseService.update(course)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (courseService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
