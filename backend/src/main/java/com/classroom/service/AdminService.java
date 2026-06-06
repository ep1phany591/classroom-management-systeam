package com.classroom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.classroom.entity.Admin;
import com.classroom.mapper.AdminMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final AdminMapper adminMapper;
    private final PasswordService passwordService;

    public AdminService(AdminMapper adminMapper, PasswordService passwordService) {
        this.adminMapper = adminMapper;
        this.passwordService = passwordService;
    }

    public List<Admin> list() {
        return adminMapper.selectList(null);
    }

    public Admin getById(Long id) {
        return adminMapper.selectById(id);
    }

    public boolean add(Admin admin) {
        admin.setPassword(passwordService.hashIfPresent(admin.getPassword()));
        return adminMapper.insert(admin) > 0;
    }

    public boolean update(Admin admin) {
        admin.setPassword(passwordService.hashIfPresent(admin.getPassword()));
        return adminMapper.updateById(admin) > 0;
    }

    public boolean delete(Long id) {
        return adminMapper.deleteById(id) > 0;
    }
}
