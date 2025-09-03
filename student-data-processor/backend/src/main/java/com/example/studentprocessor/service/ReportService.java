package com.example.studentprocessor.service;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final StudentRepository studentRepository;

    @Value("${app.file.excel-output-path:C:/var/log/applications/API/dataprocessing/}")
    private String outputPath;

    @Autowired
    public ReportService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // 1. Pagination
    public Page<Student> getStudents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findAll(pageable);
    }

    // 2. Search by StudentId
    public Student findByStudentId(Long studentId) {
        return studentRepository.findByStudentId(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
    }

    // 3. Filter by Class
    public Page<Student> filterByClass(String className, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return studentRepository.findByClassName(className, pageable);
    }

    // Helper method for getting student reports with filters
    public Page<Student> getStudentReports(Long studentId, String className, int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() :
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        if (studentId != null) {
            Student student = findByStudentId(studentId);
            return new org.springframework.data.domain.PageImpl<>(List.of(student), pageable, 1);
        } else if (className != null) {
            return studentRepository.findByClassName(className, pageable);
        } else {
            return studentRepository.findAll(pageable);
        }
    }

    public String exportToExcel(Long studentId, String className) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(outputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("student_report_%s.xlsx", timestamp);
        String fullPath = outputPath + fileName;

        // Get filtered data
        Page<Student> students = getStudentReports(studentId, className, 0, Integer.MAX_VALUE, "studentId", "asc");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Student Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] headers = {"Student ID", "First Name", "Last Name", "Date of Birth", "Class", "Score"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Add data rows
            int rowNum = 1;
            for (Student student : students.getContent()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getDob().toString());
                row.createCell(4).setCellValue(student.getClassName());
                row.createCell(5).setCellValue(student.getScore());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(fullPath)) {
                workbook.write(fileOut);
            }
        }

        return fileName;
    }

    public String exportToCsv(Long studentId, String className) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(outputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("student_report_%s.csv", timestamp);
        String fullPath = outputPath + fileName;

        // Get filtered data
        Page<Student> students = getStudentReports(studentId, className, 0, Integer.MAX_VALUE, "studentId", "asc");

        try (FileWriter fileWriter = new FileWriter(fullPath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            // Write header
            String[] headers = {"Student ID", "First Name", "Last Name", "Date of Birth", "Class", "Score"};
            csvWriter.writeNext(headers);

            // Write data
            for (Student student : students.getContent()) {
                String[] data = {
                    student.getStudentId().toString(),
                    student.getFirstName(),
                    student.getLastName(),
                    student.getDob().toString(),
                    student.getClassName(),
                    student.getScore().toString()
                };
                csvWriter.writeNext(data);
            }
        }

        return fileName;
    }

    public String exportToPdf(Long studentId, String className) throws IOException, DocumentException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(outputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("student_report_%s.pdf", timestamp);
        String fullPath = outputPath + fileName;

        // Get filtered data
        Page<Student> students = getStudentReports(studentId, className, 0, Integer.MAX_VALUE, "studentId", "asc");

        // Create and write PDF document
        try (FileOutputStream fileOut = new FileOutputStream(fullPath)) {
            Document document = new Document();
            PdfWriter.getInstance(document, fileOut);
            document.open();

            // Add title
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Student Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" ")); // Space

            // Create table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // Add headers
            com.itextpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            String[] headers = {"Student ID", "First Name", "Last Name", "Date of Birth", "Class", "Score"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                table.addCell(cell);
            }

            // Add data
            com.itextpdf.text.Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (Student student : students.getContent()) {
                table.addCell(new Phrase(student.getStudentId().toString(), dataFont));
                table.addCell(new Phrase(student.getFirstName(), dataFont));
                table.addCell(new Phrase(student.getLastName(), dataFont));
                table.addCell(new Phrase(student.getClassName(), dataFont));
                table.addCell(new Phrase(student.getScore().toString(), dataFont));
            }

            document.add(table);
            document.close();
        }

        return fileName;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return headerStyle;
    }

    // Enhanced search with multiple filters
    public Map<String, Object> getAdvancedSearchResults(
            int page, int size, String sortBy, String sortDir,
            String search, Integer minScore, Integer maxScore, String className) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() :
                   Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Student> studentPage;

        // Apply filters
        if (search != null || minScore != null || maxScore != null || className != null) {
            studentPage = studentRepository.findByAdvancedFilters(
                search, minScore, maxScore, className, pageable);
        } else {
            studentPage = studentRepository.findAll(pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("students", studentPage.getContent());
        result.put("currentPage", studentPage.getNumber());
        result.put("totalItems", studentPage.getTotalElements());
        result.put("totalPages", studentPage.getTotalPages());
        result.put("pageSize", studentPage.getSize());
        result.put("hasNext", studentPage.hasNext());
        result.put("hasPrevious", studentPage.hasPrevious());
        result.put("isFirst", studentPage.isFirst());
        result.put("isLast", studentPage.isLast());

        return result;
    }

    // Get report statistics
    public Map<String, Object> getReportStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalStudents = studentRepository.count();
        stats.put("totalStudents", totalStudents);

        if (totalStudents > 0) {
            Double avgScore = studentRepository.findAverageScore();
            Integer maxScore = studentRepository.findMaxScore();
            Integer minScore = studentRepository.findMinScore();
            Long passCount = studentRepository.countByScoreGreaterThanEqual(50);
            Long failCount = totalStudents - passCount;

            stats.put("averageScore", avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0);
            stats.put("maxScore", maxScore != null ? maxScore : 0);
            stats.put("minScore", minScore != null ? minScore : 0);
            stats.put("passCount", passCount);
            stats.put("failCount", failCount);
            stats.put("passRate", Math.round((passCount * 100.0 / totalStudents) * 100.0) / 100.0);
        } else {
            stats.put("averageScore", 0.0);
            stats.put("maxScore", 0);
            stats.put("minScore", 0);
            stats.put("passCount", 0L);
            stats.put("failCount", 0L);
            stats.put("passRate", 0.0);
        }

        return stats;
    }

    // Get score distribution for charts
    public Map<String, Object> getScoreDistribution() {
        Map<String, Object> distribution = new HashMap<>();

        // Score ranges: 0-40 (Fail), 41-60 (Pass), 61-80 (Good), 81-100 (Excellent)
        Long failCount = studentRepository.countByScoreBetween(0, 40);
        Long passCount = studentRepository.countByScoreBetween(41, 60);
        Long goodCount = studentRepository.countByScoreBetween(61, 80);
        Long excellentCount = studentRepository.countByScoreBetween(81, 100);

        distribution.put("ranges", List.of("0-40 (Fail)", "41-60 (Pass)", "61-80 (Good)", "81-100 (Excellent)"));
        distribution.put("counts", List.of(failCount, passCount, goodCount, excellentCount));
        distribution.put("colors", List.of("#ef4444", "#f59e0b", "#10b981", "#3b82f6"));

        return distribution;
    }

    // Get top performers
    public List<Student> getTopPerformers(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "score"));
        return studentRepository.findAll(pageable).getContent();
    }

    // Get available classes
    public List<String> getAvailableClasses() {
        return studentRepository.findDistinctClassNames();
    }
}