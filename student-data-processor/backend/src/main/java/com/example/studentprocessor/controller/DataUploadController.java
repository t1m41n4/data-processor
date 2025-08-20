package com.example.studentprocessor.controller;

import com.example.studentprocessor.service.DataUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class DataUploadController {

    private final DataUploadService dataUploadService;

    @Autowired
    public DataUploadController(DataUploadService dataUploadService) {
        this.dataUploadService = dataUploadService;
    }

    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (!isCsvFile(file)) {
                response.put("success", false);
                response.put("message", "Please upload a valid CSV file (.csv)");
                return ResponseEntity.badRequest().body(response);
            }

            String result = dataUploadService.uploadCsvFile(file);

            // Extract statistics from result string
            String[] parts = result.split(": ")[1].split(", ");
            int totalRecords = Integer.parseInt(parts[0].split(" ")[0]);
            int skippedRecords = Integer.parseInt(parts[1].split(" ")[0]);
            int newRecords = totalRecords - skippedRecords;

            response.put("success", true);
            response.put("message", result);
            response.put("totalRecords", totalRecords);
            response.put("newRecords", newRecords);
            response.put("duplicateRecords", skippedRecords);
            response.put("scoreAdjustment", "+5 points added to all scores");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error uploading CSV file: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getUploadStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Data Upload Service");
        response.put("status", "Available");
        response.put("supportedFormats", new String[]{"csv"});
        response.put("scoreAdjustment", "+5");
        return ResponseEntity.ok(response);
    }

    private boolean isCsvFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}