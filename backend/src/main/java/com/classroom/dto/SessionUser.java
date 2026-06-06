package com.classroom.dto;

public record SessionUser(Long id, String username, String name, String role) {
    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public boolean isTeacher() {
        return "teacher".equals(role);
    }
}
