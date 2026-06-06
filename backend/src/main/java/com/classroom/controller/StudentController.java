package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Student;
import com.classroom.service.StudentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/list")
    public Result<List<Student>> list(@RequestParam(required = false) String name) {
        return Result.success(studentService.list(name));
    }

    @GetMapping("/{id}")
    public Result<Student> getById(@PathVariable Long id) {
        return Result.success(studentService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Student student) {
        if (studentService.add(student)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Student student) {
        if (studentService.update(student)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (studentService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
