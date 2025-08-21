package com.example.studentprocessor.controller;

import com.example.studentprocessor.dto.ProcessingResult;
import com.example.studentprocessor.service.DataProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/process")
@CrossOrigin(origins = "http://localhost:4200")
public class DataProcessingController {

    private final DataProcessingService dataProcessingService;

    @Autowired
    public DataProcessingController(DataProcessingService dataProcessingService) {
        this.dataProcessingService = dataProcessingService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> processExcelToCsv(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        // Log memory status before processing
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long usedMemory = totalMemory - freeMemory;

        System.out.println("=== Excel to CSV Processing Started ===");
        System.out.println(String.format("File: %s (%.2f MB)",
            file.getOriginalFilename(), file.getSize() / (1024.0 * 1024.0)));
        System.out.println(String.format("Memory status - Max: %d MB, Used: %d MB, Available: %d MB",
            maxMemory, usedMemory, maxMemory - usedMemory));

        try {
            if (!isExcelFile(file)) {
                response.put("success", false);
                response.put("message", "Please upload an Excel file (.xlsx or .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            if (file.getSize() > 100 * 1024 * 1024) { // 100MB limit
                response.put("success", false);
                response.put("message", "File too large. Maximum size is 100MB");
                return ResponseEntity.badRequest().body(response);
            }

            long startTime = System.currentTimeMillis();
            ProcessingResult result = dataProcessingService.convertExcelToCsv(file);
            long endTime = System.currentTimeMillis();

            double processingTime = (endTime - startTime) / 1000.0;

            // Final memory status
            long finalUsedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
            System.out.println(String.format("Processing completed in %.2f seconds", processingTime));
            System.out.println(String.format("Final memory usage: %d MB", finalUsedMemory));

            response.put("success", true);
            response.put("message", "Excel file successfully converted to CSV with +10 score adjustment applied to all records");
            response.put("csvFilePath", "C:/var/log/applications/API/dataprocessing/" + result.getCsvFileName());
            response.put("processingTime", String.format("%.2f seconds", processingTime));
            response.put("recordsProcessed", result.getRecordsProcessed());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Error during Excel to CSV processing:");
            e.printStackTrace();

            response.put("success", false);
            response.put("message", "Server error during processing. Please check the file format and try again.");
            response.put("error", e.getClass().getSimpleName());
            response.put("details", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getProcessingStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Data Processing Service");
        response.put("status", "Available");
        response.put("supportedFormats", new String[]{"xlsx", "xls"});
        response.put("scoreAdjustment", "+10");
        return ResponseEntity.ok(response);
    }

    private boolean isExcelFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && (filename.endsWith(".xlsx") || filename.endsWith(".xls"));
    }
}
