package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Semester;
import com.classroom.service.SemesterService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semester")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @GetMapping("/list")
    public Result<List<Semester>> list() {
        return Result.success(semesterService.list());
    }

    @GetMapping("/{id}")
    public Result<Semester> getById(@PathVariable Long id) {
        return Result.success(semesterService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Semester semester) {
        if (semesterService.add(semester)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Semester semester) {
        if (semesterService.update(semester)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (semesterService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
