package com.example.studentprocessor.controller;

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

        try {
            if (!isExcelFile(file)) {
                response.put("success", false);
                response.put("message", "Please upload an Excel file (.xlsx or .xls)");
                return ResponseEntity.badRequest().body(response);
            }

            String csvFileName = dataProcessingService.convertExcelToCsv(file);

            response.put("success", true);
            response.put("message", "CSV file created successfully: " + csvFileName);
            response.put("csvFilePath", "C:/var/log/applications/API/dataprocessing/" + csvFileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error processing Excel file: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

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
