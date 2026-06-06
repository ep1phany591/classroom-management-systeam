package com.classroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.classroom.dto.LoginRequest;
import com.classroom.dto.LoginResult;
import com.classroom.dto.SessionUser;
import com.classroom.entity.Admin;
import com.classroom.entity.Student;
import com.classroom.entity.Teacher;
import com.classroom.mapper.AdminMapper;
import com.classroom.mapper.StudentMapper;
import com.classroom.mapper.TeacherMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private static final int MAX_LOGIN_LENGTH = 64;
    private static final long SESSION_HOURS = 8;

    private final AdminMapper adminMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final PasswordService passwordService;
    private final InputSecurityService inputSecurityService;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(AdminMapper adminMapper, TeacherMapper teacherMapper, StudentMapper studentMapper,
                       PasswordService passwordService, InputSecurityService inputSecurityService) {
        this.adminMapper = adminMapper;
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.passwordService = passwordService;
        this.inputSecurityService = inputSecurityService;
    }

    public LoginResult login(LoginRequest request) {
        if (request == null || !StringUtils.hasText(request.getUsername())
                || !StringUtils.hasText(request.getPassword()) || !StringUtils.hasText(request.getRole())) {
            throw new IllegalArgumentException("账号、密码和角色不能为空");
        }
        String username = inputSecurityService.validateUsername(request.getUsername());
        String password = request.getPassword();
        String role = request.getRole();
        if (username.length() > MAX_LOGIN_LENGTH || password.length() > MAX_LOGIN_LENGTH) {
            throw new IllegalArgumentException("账号或密码长度不合法");
        }

        SessionUser user = null;
        if ("admin".equals(role)) {
            Admin admin = adminMapper.selectOne(
                    new QueryWrapper<Admin>().eq("username", username));
            if (admin != null && passwordService.matches(password, admin.getPassword())) {
                if (passwordService.needsUpgrade(admin.getPassword())) {
                    admin.setPassword(passwordService.hashIfPresent(password));
                    adminMapper.updateById(admin);
                }
                user = new SessionUser(admin.getId(), admin.getUsername(), admin.getName(), "admin");
            }
        } else if ("teacher".equals(role)) {
            Teacher teacher = teacherMapper.selectOne(
                    new QueryWrapper<Teacher>().eq("username", username));
            if (teacher != null && passwordService.matches(password, teacher.getPassword())) {
                if (passwordService.needsUpgrade(teacher.getPassword())) {
                    teacher.setPassword(passwordService.hashIfPresent(password));
                    teacherMapper.updateById(teacher);
                }
                user = new SessionUser(teacher.getId(), teacher.getUsername(), teacher.getName(), "teacher");
            }
        } else if ("student".equals(role)) {
            Student student = studentMapper.selectOne(
                    new QueryWrapper<Student>().eq("username", username));
            if (student != null && passwordService.matches(password, student.getPassword())) {
                if (passwordService.needsUpgrade(student.getPassword())) {
                    student.setPassword(passwordService.hashIfPresent(password));
                    studentMapper.updateById(student);
                }
                user = new SessionUser(student.getId(), student.getUsername(), student.getName(), "student");
            }
        } else {
            throw new IllegalArgumentException("角色不合法");
        }
        if (user == null) {
            return null;
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, new Session(user, Instant.now().plus(SESSION_HOURS, ChronoUnit.HOURS)));
        return new LoginResult(user.id(), user.username(), user.name(), user.role(), token);
    }

    public SessionUser authenticate(String token) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        Session session = sessions.get(token);
        if (session == null || session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return null;
        }
        return session.user();
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private record Session(SessionUser user, Instant expiresAt) {}
}
