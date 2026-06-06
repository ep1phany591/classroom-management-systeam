package com.classroom.dto;

public class LoginResult {
    private Long id;
    private String username;
    private String name;
    private String role;
    private String token;

    public LoginResult() {}

    public LoginResult(Long id, String username, String name, String role, String token) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.role = role;
        this.token = token;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
