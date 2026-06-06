package com.classroom.controller;

import com.classroom.dto.LoginRequest;
import com.classroom.dto.LoginResult;
import com.classroom.dto.Result;
import com.classroom.service.AuthService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest request) {
        LoginResult result = authService.login(request);
        if (result != null) {
            return Result.success("登录成功", result);
        }
        return Result.error(401, "用户名或密码错误");
    }

    @PostMapping("/logout")
    public Result<String> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("X-Auth-Token"));
        return Result.success("退出成功");
    }
}
