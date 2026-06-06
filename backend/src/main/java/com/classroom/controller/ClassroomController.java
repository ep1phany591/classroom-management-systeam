package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Classroom;
import com.classroom.service.ClassroomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classroom")
public class ClassroomController {

    private final ClassroomService classroomService;

    public ClassroomController(ClassroomService classroomService) {
        this.classroomService = classroomService;
    }

    @GetMapping("/list")
    public Result<List<Classroom>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String building) {
        return Result.success(classroomService.list(name, building));
    }

    @GetMapping("/{id}")
    public Result<Classroom> getById(@PathVariable Long id) {
        return Result.success(classroomService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Classroom classroom) {
        if (classroomService.add(classroom)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Classroom classroom) {
        if (classroomService.update(classroom)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (classroomService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
