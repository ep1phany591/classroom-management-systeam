package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Teacher;
import com.classroom.service.TeacherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/list")
    public Result<List<Teacher>> list(@RequestParam(required = false) String name) {
        return Result.success(teacherService.list(name));
    }

    @GetMapping("/{id}")
    public Result<Teacher> getById(@PathVariable Long id) {
        return Result.success(teacherService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Teacher teacher) {
        if (teacherService.add(teacher)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Teacher teacher) {
        if (teacherService.update(teacher)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (teacherService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
