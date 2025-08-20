package com.example.studentprocessor.controller;

import com.example.studentprocessor.service.DataGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "http://localhost:4200")
public class DataGenerationController {

    private final DataGenerationService dataGenerationService;
    private static final int MAX_RECORDS = 2000000; // Safety limit for memory

    @Autowired
    public DataGenerationController(DataGenerationService dataGenerationService) {
        this.dataGenerationService = dataGenerationService;
    }

    @PostMapping
    public ResponseEntity<String> generateData(@RequestParam int recordCount) {
        try {
            // Validate record count
            if (recordCount <= 0) {
                return ResponseEntity.badRequest().body("Record count must be positive");
            }

            if (recordCount > MAX_RECORDS) {
                return ResponseEntity.badRequest().body(
                    String.format("Record count cannot exceed %,d for memory safety", MAX_RECORDS));
            }

            // Check available memory before generation
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long availableMemory = maxMemory - totalMemory + freeMemory;

            System.out.printf("Memory status before generation:%n");
            System.out.printf("Max memory: %.1f MB%n", maxMemory / 1024.0 / 1024.0);
            System.out.printf("Available memory: %.1f MB%n", availableMemory / 1024.0 / 1024.0);

            String fileName = dataGenerationService.generateStudentExcelFile(recordCount);

            // Memory status after generation
            long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
            System.out.printf("Memory used after generation: %.1f MB%n", usedMemoryAfter / 1024.0 / 1024.0);

            return ResponseEntity.ok("Excel file generated successfully: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating file: " + e.getMessage());
        }
    }
}
