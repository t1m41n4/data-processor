package com.example.studentprocessor.controller;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import com.example.studentprocessor.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:4200")
public class ReportController {

    private final ReportService reportService;
    private final StudentRepository studentRepository;

    @Autowired
    public ReportController(ReportService reportService, StudentRepository studentRepository) {
        this.reportService = reportService;
        this.studentRepository = studentRepository;
    }

    // 1. Pagination
    @GetMapping
    public ResponseEntity<Page<Student>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(reportService.getStudents(page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 2. Search by StudentId
    @GetMapping("/search")
    public ResponseEntity<Student> searchByStudentId(@RequestParam Long studentId) {
        try {
            return ResponseEntity.ok(reportService.findByStudentId(studentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 3. Filter by Class
    @GetMapping("/filter")
    public ResponseEntity<Page<Student>> filterByClass(
            @RequestParam String className,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            return ResponseEntity.ok(reportService.filterByClass(className, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // 4. Get 10th Record
    @GetMapping("/tenth-record")
    public ResponseEntity<Student> getTenthRecord() {
        try {
            Student tenthRecord = reportService.getTenthRecord();
            if (tenthRecord != null) {
                return ResponseEntity.ok(tenthRecord);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/export/excel")
    public ResponseEntity<Map<String, Object>> exportToExcel(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className) {

        Map<String, Object> response = new HashMap<>();

        try {
            String fileName = reportService.exportToExcel(studentId, className);

            response.put("success", true);
            response.put("message", "Excel report generated successfully");
            response.put("fileName", fileName);
            response.put("filePath", "C:/var/log/applications/API/dataprocessing/" + fileName);
            response.put("format", "Excel");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating Excel report: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/export/csv")
    public ResponseEntity<Map<String, Object>> exportToCsv(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className) {

        Map<String, Object> response = new HashMap<>();

        try {
            String fileName = reportService.exportToCsv(studentId, className);

            response.put("success", true);
            response.put("message", "CSV report generated successfully");
            response.put("fileName", fileName);
            response.put("filePath", "C:/var/log/applications/API/dataprocessing/" + fileName);
            response.put("format", "CSV");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating CSV report: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<Map<String, Object>> exportToPdf(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String className) {

        Map<String, Object> response = new HashMap<>();

        try {
            String fileName = reportService.exportToPdf(studentId, className);

            response.put("success", true);
            response.put("message", "PDF report generated successfully");
            response.put("fileName", fileName);
            response.put("filePath", "C:/var/log/applications/API/dataprocessing/" + fileName);
            response.put("format", "PDF");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error generating PDF report: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getReportStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Report Service");
        response.put("status", "Available");
        response.put("features", new String[]{"Pagination", "Search by Student ID", "Filter by Class", "Export to Excel/CSV/PDF"});
        response.put("exportFormats", new String[]{"xlsx", "csv", "pdf"});
        return ResponseEntity.ok(response);
    }

    // Enhanced search with multiple filters
    @GetMapping("/advanced-search")
    public ResponseEntity<Map<String, Object>> advancedSearch(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "studentId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer minScore,
            @RequestParam(required = false) Integer maxScore,
            @RequestParam(required = false) String className) {

        try {
            Map<String, Object> result = reportService.getAdvancedSearchResults(
                page, size, sortBy, sortDir, search, minScore, maxScore, className);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to perform advanced search: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get report statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = reportService.getReportStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get score distribution for charts
    @GetMapping("/score-distribution")
    public ResponseEntity<Map<String, Object>> getScoreDistribution() {
        try {
            Map<String, Object> distribution = reportService.getScoreDistribution();
            return ResponseEntity.ok(distribution);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch score distribution: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // Get top performers
    @GetMapping("/top-performers")
    public ResponseEntity<List<Student>> getTopPerformers(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<Student> topPerformers = reportService.getTopPerformers(limit);
            return ResponseEntity.ok(topPerformers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Get available classes
    @GetMapping("/classes")
    public ResponseEntity<List<String>> getAvailableClasses() {
        try {
            List<String> classes = reportService.getAvailableClasses();
            return ResponseEntity.ok(classes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Clear all data from the database
    @DeleteMapping("/clear-data")
    public ResponseEntity<Map<String, Object>> clearAllData() {
        try {
            long recordsDeleted = studentRepository.count();
            studentRepository.deleteAll();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Database cleared successfully");
            response.put("recordsDeleted", recordsDeleted);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to clear database: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}