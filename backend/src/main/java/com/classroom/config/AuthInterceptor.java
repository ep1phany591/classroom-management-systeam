package com.classroom.config;

import com.classroom.dto.SessionUser;
import com.classroom.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String token = request.getHeader("X-Auth-Token");
        SessionUser user = authService.authenticate(token);
        if (user == null) {
            return reject(response, 401, "请先登录或重新登录");
        }
        request.setAttribute("sessionUser", user);
        if (!canAccess(request, user)) {
            return reject(response, 403, "没有权限执行此操作");
        }
        return true;
    }

    private boolean canAccess(HttpServletRequest request, SessionUser user) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (path.startsWith("/api/database-module")) {
            return user.isAdmin();
        }
        if ("GET".equals(method) || path.equals("/api/auth/logout")) {
            return true;
        }
        if (path.startsWith("/api/borrow")) {
            if ("DELETE".equals(method)) {
                return user.isAdmin();
            }
            return user.isAdmin() || user.isTeacher();
        }
        return user.isAdmin();
    }

    private boolean reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + status + ",\"message\":\"" + message + "\"}");
        return false;
    }
}
