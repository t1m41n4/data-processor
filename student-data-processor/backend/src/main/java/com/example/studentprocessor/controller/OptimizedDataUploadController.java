package com.example.studentprocessor.controller;

import com.example.studentprocessor.service.OptimizedDataUploadService;
import com.example.studentprocessor.service.UltraHighPerformanceService;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @PostMapping("/csv/ultra-fast")
    public ResponseEntity<Map<String, Object>> uploadCsvFileUltraFast(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("⚡ ULTRA-FAST MODE: " + file.getOriginalFilename() +
                             " (" + String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0)) + ")");

            OptimizedDataUploadService.UploadResult result = ultraHighPerformanceService.uploadCsvUltraFast(file);

            response.put("success", true);
            response.put("totalRecords", result.getTotalRecords());
            response.put("newRecords", result.getNewRecords());
            response.put("skippedRecords", result.getSkippedRecords());
            response.put("processingTime", result.getProcessingTime());
            response.put("verificationMessage", result.getVerificationMessage());
            response.put("message", "⚡ Ultra-fast processing completed!");

            double recordsPerSecond = result.getTotalRecords() / (result.getProcessingTime() / 1000.0);
            response.put("recordsPerSecond", Math.round(recordsPerSecond));

            System.out.println("⚡ Ultra-fast upload completed successfully");

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            System.err.println("❌ IO Error during ultra-fast upload: " + e.getMessage());
            response.put("success", false);
            response.put("message", "IO Error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);

        } catch (Exception e) {
            System.err.println("❌ Error during ultra-fast upload: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Processing error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/csv/optimized")
    public ResponseEntity<Map<String, Object>> uploadCsvFileOptimized(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("🚀 Starting optimized CSV upload: " + file.getOriginalFilename() +
                             " (" + String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0)) + ")");

            OptimizedDataUploadService.UploadResult result = optimizedDataUploadService.uploadCsvFileOptimized(file);

            response.put("success", true);
            response.put("totalRecords", result.getTotalRecords());
            response.put("newRecords", result.getNewRecords());
            response.put("skippedRecords", result.getSkippedRecords());
            response.put("processingTime", result.getProcessingTime());
            response.put("verificationMessage", result.getVerificationMessage());
            response.put("message", "File uploaded and processed successfully with optimizations!");

            System.out.println("✅ Optimized upload completed successfully");

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

    // Backwards-compatible alias for old clients that still POST to /api/upload/csv
    @PostMapping("/csv")
    public ResponseEntity<Map<String, Object>> uploadCsvFileAlias(@RequestParam("file") MultipartFile file) {
        // Delegate to the optimized handler to keep a single processing path and avoid code duplication
        return uploadCsvFileOptimized(file);
    }

    @GetMapping("/progress")
    public ResponseEntity<Map<String, Object>> getUploadProgress() {
        Map<String, Object> response = new HashMap<>();
        response.put("progress", optimizedDataUploadService.getProgress());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/progress/ultra-fast")
    public ResponseEntity<Map<String, Object>> getUltraFastProgress() {
        Map<String, Object> response = new HashMap<>();
        response.put("progress", ultraHighPerformanceService.getProgress());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cancel")
    @Transactional
    public ResponseEntity<Map<String, Object>> cancelUpload() {
        Map<String, Object> response = new HashMap<>();

        // Get progress before cancellation
        int optimizedProgress = optimizedDataUploadService.getProgress();
        int ultraProgress = ultraHighPerformanceService.getProgress();

        // Cancel the uploads
        optimizedDataUploadService.cancelUpload();
        ultraHighPerformanceService.cancelUpload();

        response.put("success", true);
        response.put("optimizedProgress", optimizedProgress);
        response.put("ultraProgress", ultraProgress);
        response.put("message", "Upload cancelled. " +
                   Math.max(optimizedProgress, ultraProgress) + "% of data has been saved to database.");

        System.out.println("🛑 Upload cancelled by user request. Progress was: Optimized=" +
                         optimizedProgress + "%, Ultra=" + ultraProgress + "%");

        return ResponseEntity.ok(response);
    }
}
