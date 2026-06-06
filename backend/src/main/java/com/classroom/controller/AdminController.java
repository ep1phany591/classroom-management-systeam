package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Admin;
import com.classroom.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/list")
    public Result<List<Admin>> list() {
        return Result.success(adminService.list());
    }

    @GetMapping("/{id}")
    public Result<Admin> getById(@PathVariable Long id) {
        return Result.success(adminService.getById(id));
    }

    @PostMapping
    public Result<String> add(@RequestBody Admin admin) {
        if (adminService.add(admin)) {
            return Result.success("添加成功");
        }
        return Result.error("添加失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody Admin admin) {
        if (adminService.update(admin)) {
            return Result.success("修改成功");
        }
        return Result.error("修改失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        if (adminService.delete(id)) {
            return Result.success("删除成功");
        }
        return Result.error("删除失败");
    }
}
