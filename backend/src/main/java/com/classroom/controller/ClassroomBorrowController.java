package com.classroom.controller;

import com.classroom.dto.Result;
import com.classroom.dto.SessionUser;
import com.classroom.entity.ClassroomBorrow;
import com.classroom.service.ClassroomBorrowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/borrow")
public class ClassroomBorrowController {

    private final ClassroomBorrowService borrowService;

    public ClassroomBorrowController(ClassroomBorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/list")
    public Result<List<ClassroomBorrow>> list(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) String date) {
        return Result.success(borrowService.list(classroomId, teacherId, date));
    }

    @PostMapping
    public Result<String> add(@RequestBody ClassroomBorrow borrow, HttpServletRequest request) {
        SessionUser user = sessionUser(request);
        if (user.isTeacher()) {
            borrow.setTeacherId(user.id());
        }
        if (borrowService.add(borrow)) {
            return Result.success("借用成功");
        }
        return Result.error("该时段教室已被占用，借用失败");
    }

    @PutMapping("/cancel/{id}")
    public Result<String> cancel(@PathVariable Long id, HttpServletRequest request) {
        SessionUser user = sessionUser(request);
        ClassroomBorrow borrow = borrowService.getById(id);
        if (borrow == null) {
            return Result.error(404, "借用记录不存在");
        }
        if (user.isTeacher() && !user.id().equals(borrow.getTeacherId())) {
            return Result.error(403, "只能取消自己的借用记录");
        }
        return borrowService.cancel(id) ? Result.success("取消成功") : Result.error("取消失败");
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return borrowService.delete(id) ? Result.success("删除成功") : Result.error("删除失败");
    }

    @GetMapping("/daily-free")
    public Result<List<Map<String, Object>>> dailyFree(@RequestParam String date) {
        return Result.success(borrowService.dailyFree(date));
    }

    @GetMapping("/weekly-free")
    public Result<List<Map<String, Object>>> weeklyFree() {
        return Result.success(borrowService.weeklyFree());
    }

    @GetMapping("/daily-schedule")
    public Result<List<ClassroomBorrow>> dailySchedule(@RequestParam String date) {
        return Result.success(borrowService.dailySchedule(date));
    }

    @GetMapping("/weekly-schedule")
    public Result<List<Map<String, Object>>> weeklySchedule() {
        return Result.success(borrowService.weeklySchedule());
    }

    private SessionUser sessionUser(HttpServletRequest request) {
        return (SessionUser) request.getAttribute("sessionUser");
    }
}
