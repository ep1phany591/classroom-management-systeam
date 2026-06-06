package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.entity.Classroom;
import com.classroom.entity.ClassroomBorrow;
import com.classroom.service.DatabaseModuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/database-module")
public class DatabaseModuleController {

    private final DatabaseModuleService databaseModuleService;

    public DatabaseModuleController(DatabaseModuleService databaseModuleService) {
        this.databaseModuleService = databaseModuleService;
    }

    @GetMapping("/classrooms")
    public Result<List<Map<String, Object>>> listClassrooms(@RequestParam(required = false) String name) {
        return Result.success(databaseModuleService.listClassrooms(name));
    }

    @GetMapping("/classrooms/{id}")
    public Result<List<Map<String, Object>>> getClassroom(@PathVariable Long id) {
        return Result.success(databaseModuleService.getClassroom(id));
    }

    @PostMapping("/classrooms")
    public Result<List<Map<String, Object>>> createClassroom(@RequestBody Classroom classroom) {
        return Result.success("存储过程新增成功", databaseModuleService.createClassroom(classroom));
    }

    @PutMapping("/classrooms")
    public Result<List<Map<String, Object>>> updateClassroom(@RequestBody Classroom classroom) {
        return Result.success("存储过程修改成功", databaseModuleService.updateClassroom(classroom));
    }

    @DeleteMapping("/classrooms/{id}")
    public Result<String> deleteClassroom(@PathVariable Long id) {
        databaseModuleService.deleteClassroom(id);
        return Result.success("存储过程删除成功");
    }

    @PostMapping("/borrows/transaction")
    public Result<List<Map<String, Object>>> createBorrowTransaction(@RequestBody ClassroomBorrow borrow) {
        return Result.success("事务借用成功", databaseModuleService.createBorrowTransaction(borrow));
    }

    @PutMapping("/borrows/{id}/cancel-transaction")
    public Result<List<Map<String, Object>>> cancelBorrowTransaction(@PathVariable Long id) {
        return Result.success("事务取消成功", databaseModuleService.cancelBorrowTransaction(id));
    }

    @DeleteMapping("/borrows/{id}/transaction")
    public Result<String> deleteBorrowTransaction(@PathVariable Long id) {
        databaseModuleService.deleteBorrowTransaction(id);
        return Result.success("事务删除成功");
    }

    @GetMapping("/audit-logs")
    public Result<List<Map<String, Object>>> listAuditLogs(@RequestParam(required = false) Integer limit) {
        return Result.success(databaseModuleService.listAuditLogs(limit));
    }

    @GetMapping("/analysis")
    public Result<List<Map<String, Object>>> analyzeUsage(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(databaseModuleService.analyzeUsage(startDate, endDate));
    }

    @GetMapping("/analysis-view")
    public Result<List<Map<String, Object>>> readUsageView() {
        return Result.success(databaseModuleService.readUsageView());
    }
}
