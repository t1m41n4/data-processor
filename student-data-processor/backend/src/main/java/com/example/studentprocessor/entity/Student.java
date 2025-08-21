package com.example.studentprocessor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDate;

@Entity
@Table(name = "students",
       indexes = {
           @Index(name = "idx_student_id", columnList = "student_id"),
           @Index(name = "idx_class_name", columnList = "class_name"),
           @Index(name = "idx_score", columnList = "score")
       })
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", unique = true, nullable = false)
    @NotNull
    private Long studentId;

    @Column(name = "first_name", nullable = false, length = 50)
    @NotNull
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    @NotNull
    private String lastName;

    @Column(name = "dob", nullable = false)
    @NotNull
    private LocalDate dob;

    @Column(name = "class_name", nullable = false, length = 20)
    @NotNull
    private String className;

    @Column(name = "score", nullable = false)
    @NotNull
    @Min(value = 0)
    @Max(value = 100)
    private Integer score;


    // Default constructor
    public Student() {}

    // Constructor with all fields
    public Student(Long studentId, String firstName, String lastName, LocalDate dob, String className, Integer score) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.className = className;
        this.score = score;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", studentId=" + studentId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dob=" + dob +
                ", className='" + className + '\'' +
                ", score=" + score +
                '}';
    }
}