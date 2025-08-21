package com.example.studentprocessor.controller;

import com.example.studentprocessor.service.OptimizedDataUploadService;
import com.example.studentprocessor.service.UltraHighPerformanceService;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@CrossOrigin(origins = "http://localhost:4200")
public class OptimizedDataUploadController {

    private final OptimizedDataUploadService optimizedDataUploadService;
    private final UltraHighPerformanceService ultraHighPerformanceService;

    @Autowired
    public OptimizedDataUploadController(OptimizedDataUploadService optimizedDataUploadService,
                                       UltraHighPerformanceService ultraHighPerformanceService) {
        this.optimizedDataUploadService = optimizedDataUploadService;
        this.ultraHighPerformanceService = ultraHighPerformanceService;
    }

    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Smart selection: Use ultra-fast for large files (10MB+), optimized for smaller files
            boolean useUltraFast = file.getSize() > 10 * 1024 * 1024; // 10MB threshold
            String mode = useUltraFast ? "ULTRA-FAST" : "OPTIMIZED";

            System.out.println("🚀 Starting " + mode + " CSV upload: " + file.getOriginalFilename() +
                             " (" + String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0)) + ")");

            OptimizedDataUploadService.UploadResult result;

            if (useUltraFast) {
                result = ultraHighPerformanceService.uploadCsvUltraFast(file);
                response.put("message", "⚡ Ultra-fast processing completed!");
            } else {
                result = optimizedDataUploadService.uploadCsvFileOptimized(file);
                response.put("message", "🚀 Optimized processing completed!");
            }

            response.put("success", true);
            response.put("mode", mode);
            response.put("totalRecords", result.getTotalRecords());
            response.put("newRecords", result.getNewRecords());
            response.put("skippedRecords", result.getSkippedRecords());
            response.put("processingTime", result.getProcessingTime());
            response.put("verificationMessage", result.getVerificationMessage());

            if (result.getProcessingTime() > 0) {
                double recordsPerSecond = result.getTotalRecords() / (result.getProcessingTime() / 1000.0);
                response.put("recordsPerSecond", Math.round(recordsPerSecond));
            }

            System.out.println("✅ " + mode + " upload completed successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("❌ IO Error during upload: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error reading file: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (CsvException e) {
            System.err.println("❌ CSV Error during upload: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Error parsing CSV: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            System.err.println("❌ Unexpected error during upload: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getUploadProgress() {
        Map<String, Object> response = new HashMap<>();

        // Get progress from both services and return the higher value (active one)
        int optimizedProgress = optimizedDataUploadService.getProgress();
        int ultraFastProgress = ultraHighPerformanceService.getProgress();
        int currentProgress = Math.max(optimizedProgress, ultraFastProgress);

        response.put("progress", currentProgress);
        response.put("optimizedProgress", optimizedProgress);
        response.put("ultraFastProgress", ultraFastProgress);

        return ResponseEntity.ok(response);
    }
}
