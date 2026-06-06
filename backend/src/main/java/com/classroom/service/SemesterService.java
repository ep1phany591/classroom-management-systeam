package com.classroom.service;

import com.classroom.entity.Semester;
import com.classroom.mapper.SemesterMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SemesterService {

    private final SemesterMapper semesterMapper;

    public SemesterService(SemesterMapper semesterMapper) {
        this.semesterMapper = semesterMapper;
    }

    public List<Semester> list() {
        return semesterMapper.selectList(null);
    }

    public Semester getById(Long id) {
        return semesterMapper.selectById(id);
    }

    public boolean add(Semester semester) {
        return semesterMapper.insert(semester) > 0;
    }

    public boolean update(Semester semester) {
        return semesterMapper.updateById(semester) > 0;
    }

    public boolean delete(Long id) {
        return semesterMapper.deleteById(id) > 0;
    }
}
