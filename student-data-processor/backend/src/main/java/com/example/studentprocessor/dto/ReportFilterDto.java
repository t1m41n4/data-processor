package com.example.studentprocessor.dto;

public class ReportFilterDto {

    private Long studentId;
    private String className;
    private int page = 0;
    private int size = 10;
    private String sortBy = "studentId";
    private String sortDir = "asc";

    // Default constructor
    public ReportFilterDto() {}

    // Constructor with common fields
    public ReportFilterDto(Long studentId, String className, int page, int size) {
        this.studentId = studentId;
        this.className = className;
        this.page = page;
        this.size = size;
    }

    // Getters and Setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }
}
