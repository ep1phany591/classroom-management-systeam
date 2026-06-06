package com.classroom.config;

import com.classroom.dto.Result;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleBadRequest(IllegalArgumentException exception) {
        return Result.error(400, exception.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleConflict(DataIntegrityViolationException exception) {
        return Result.error(409, "数据已存在或仍被其他记录引用");
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<String> handleDatabaseError(DataAccessException exception) {
        return Result.error(409, "数据库操作失败，请检查数据状态或关联记录");
    }
}
