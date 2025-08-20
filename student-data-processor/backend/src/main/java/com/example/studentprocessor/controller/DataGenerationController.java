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

    @Autowired
    public DataGenerationController(DataGenerationService dataGenerationService) {
        this.dataGenerationService = dataGenerationService;
    }

    @PostMapping
    public ResponseEntity<String> generateData(@RequestParam int recordCount) {
        try {
            String fileName = dataGenerationService.generateStudentExcelFile(recordCount);
            return ResponseEntity.ok("Excel file generated successfully: " + fileName);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error generating file: " + e.getMessage());
        }
    }
}
