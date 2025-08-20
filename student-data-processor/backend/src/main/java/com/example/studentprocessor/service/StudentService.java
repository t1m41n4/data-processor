package com.example.studentprocessor.service;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long studentId) {
        return studentRepository.findById(studentId);
    }

    public Optional<Student> getStudentByStudentId(Long studentId) {
        return studentRepository.findByStudentId(studentId);
    }

    public Page<Student> getStudentsWithFilters(Long studentId, String className,
                                               int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() :
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        return studentRepository.findStudentsWithFilters(studentId, className, pageable);
    }

    public List<String> getDistinctClassNames() {
        return studentRepository.findDistinctClassNames();
    }

    public void deleteStudent(Long studentId) {
        studentRepository.deleteById(studentId);
    }

    public Student updateStudent(Long studentId, Student studentDetails) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with id " + studentId));
        student.setStudentId(studentDetails.getStudentId());
        student.setFirstName(studentDetails.getFirstName());
        student.setLastName(studentDetails.getLastName());
        student.setDob(studentDetails.getDob());
        student.setClassName(studentDetails.getClassName());
        student.setScore(studentDetails.getScore());
        return studentRepository.save(student);
    }

    public void saveAll(List<Student> students) {
        studentRepository.saveAll(students);
    }

    public boolean existsByStudentId(Long studentId) {
        return studentRepository.existsByStudentId(studentId);
    }

    public long getTotalCount() {
        return studentRepository.count();
    }
}