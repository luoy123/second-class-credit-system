package com.secondclass.credit.controller;

import com.secondclass.credit.common.ApiResponse;
import com.secondclass.credit.domain.dto.StudentCreateRequest;
import com.secondclass.credit.domain.entity.Student;
import com.secondclass.credit.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    public ApiResponse<Student> create(@Valid @RequestBody StudentCreateRequest request) {
        Student student = new Student();
        student.setStudentNo(request.getStudentNo());
        student.setName(request.getName());
        student.setMajor(request.getMajor());
        student.setGrade(request.getGrade());
        return ApiResponse.success(studentService.create(student));
    }

    @GetMapping("/{id}")
    public ApiResponse<Student> findById(@PathVariable Long id) {
        return ApiResponse.success(studentService.findById(id));
    }

    @GetMapping
    public ApiResponse<List<Student>> list() {
        return ApiResponse.success(studentService.list());
    }
}
