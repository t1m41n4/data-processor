package com.example.studentprocessor.service;

import com.example.studentprocessor.dto.ProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DataProcessingService {

    @Value("${app.file.csv-output-path:C:/var/log/applications/API/dataprocessing/}")
    private String csvOutputPath;

    @Autowired
    private StreamingExcelToCsvService streamingService;

    public ProcessingResult convertExcelToCsv(MultipartFile file) throws IOException {
        System.out.println("=== Excel to CSV Processing Started ===");
        System.out.printf("File: %s (%.2f MB)%n", file.getOriginalFilename(), file.getSize() / (1024.0 * 1024.0));

        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(csvOutputPath);
        if (!Files.exists(outputDir)) {
            System.out.println("Creating output directory: " + outputDir);
            Files.createDirectories(outputDir);
        }

        // Save uploaded file temporarily
        String tempFileName = "temp_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String tempFilePath = csvOutputPath + tempFileName;

        try {
            // Save uploaded file
            file.transferTo(new File(tempFilePath));
            System.out.println("Saved temp file: " + tempFilePath);

            // Use streaming conversion for all files to avoid memory issues
            ProcessingResult result = streamingService.convertExcelToCsvStreaming(tempFilePath);

            // Clean up temp file
            Files.deleteIfExists(Paths.get(tempFilePath));
            System.out.println("Cleaned up temp file");

            return result;

        } catch (Exception e) {
            // Clean up temp file on error
            try {
                Files.deleteIfExists(Paths.get(tempFilePath));
            } catch (Exception cleanupError) {
                System.err.println("Failed to clean up temp file: " + cleanupError.getMessage());
            }

            throw new IOException("Failed to convert Excel to CSV: " + e.getMessage(), e);
        }
    }
}
