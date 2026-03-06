package com.secondclass.credit.service;

import com.secondclass.credit.domain.entity.Student;
import com.secondclass.credit.exception.BusinessException;
import com.secondclass.credit.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public Student create(Student student) {
        if (studentRepository.existsByStudentNo(student.getStudentNo())) {
            throw new BusinessException("学号已存在：" + student.getStudentNo());
        }
        return studentRepository.save(student);
    }

    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("学生不存在，id=" + id));
    }

    public List<Student> list() {
        return studentRepository.findAll();
    }
}
