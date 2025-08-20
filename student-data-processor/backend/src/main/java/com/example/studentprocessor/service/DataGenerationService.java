package com.example.studentprocessor.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DataGenerationService {

    @Value("${app.file.excel-output-path:C:/var/log/applications/API/dataprocessing/}")
    private String excelOutputPath;

    private static final String[] CLASS_OPTIONS = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private static final String[] FIRST_NAMES = generateNamePool(1000); // Pre-generated names for speed
    private static final String[] LAST_NAMES = generateNamePool(1000);
    private static final int MEMORY_ROWS = 500; // Keep only 500 rows in memory (reduced from 1000)
    private static final int FLUSH_INTERVAL = 1000; // Flush every 1000 rows for better performance

    public String generateStudentExcelFile(int recordCount) throws IOException {
        // Performance monitoring
        long startTime = System.currentTimeMillis();
        System.out.printf("Starting generation of %,d records...%n", recordCount);

        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(excelOutputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Generate unique filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("student_data_%s_%d_records.xlsx", timestamp, recordCount);
        String fullPath = excelOutputPath + fileName;

        // Use SXSSFWorkbook for streaming with minimal memory usage
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(MEMORY_ROWS)) {
            // Enable compression to reduce temp file size
            workbook.setCompressTempFiles(true);

            Sheet sheet = workbook.createSheet("Students");

            // Create header row
            createHeaderRow(sheet);

            // Generate student data rows with optimized memory management
            generateStudentDataRowsOptimized(sheet, recordCount);

            // Don't auto-size columns for large datasets (too expensive)
            // Disabled auto-sizing to prevent streaming workbook issues
            // if (recordCount <= 5000) {
            //     try {
            //         autoSizeColumns(sheet);
            //     } catch (Exception e) {
            //         System.out.println("Skipping column auto-sizing due to: " + e.getMessage());
            //     }
            // }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(fullPath)) {
                workbook.write(fileOut);
            }

            // Clean up temporary files
            workbook.dispose();
        }

        // Performance reporting
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        double recordsPerSecond = recordCount / (duration / 1000.0);

        System.out.printf("Generation completed!%n");
        System.out.printf("Records: %,d%n", recordCount);
        System.out.printf("Time taken: %.2f seconds%n", duration / 1000.0);
        System.out.printf("Speed: %.0f records/second%n", recordsPerSecond);
        System.out.printf("File: %s%n", fileName);

        return fileName;
    }

    private void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);

        // Create header style
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // Create header cells
        String[] headers = {"studentId", "firstName", "lastName", "DOB", "class", "score"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void generateStudentDataRowsOptimized(Sheet sheet, int recordCount) throws IOException {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        SXSSFSheet sxssfSheet = (SXSSFSheet) sheet;

        // Pre-calculate date range for better performance
        long startEpochDay = LocalDate.of(2000, 1, 1).toEpochDay();
        long endEpochDay = LocalDate.of(2010, 12, 31).toEpochDay();
        long dateRange = endEpochDay - startEpochDay + 1;

        for (int i = 1; i <= recordCount; i++) {
            Row row = sheet.createRow(i);

            // studentId - numeric incremental starting from 1
            row.createCell(0).setCellValue(i);

            // firstName - from pre-generated pool for speed
            row.createCell(1).setCellValue(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)]);

            // lastName - from pre-generated pool for speed
            row.createCell(2).setCellValue(LAST_NAMES[random.nextInt(LAST_NAMES.length)]);

            // DOB - optimized random date generation
            LocalDate randomDate = LocalDate.ofEpochDay(startEpochDay + random.nextLong(dateRange));
            row.createCell(3).setCellValue(randomDate.toString());

            // class - random from predefined options
            row.createCell(4).setCellValue(CLASS_OPTIONS[random.nextInt(CLASS_OPTIONS.length)]);

            // score - random number between 55 and 75
            row.createCell(5).setCellValue(55 + random.nextInt(21));

            // Aggressive memory management - flush frequently
            if (i % FLUSH_INTERVAL == 0) {
                sxssfSheet.flushRows(FLUSH_INTERVAL);

                // Progress logging for large datasets with memory info
                if (i % 10000 == 0) {
                    Runtime runtime = Runtime.getRuntime();
                    long usedMemory = runtime.totalMemory() - runtime.freeMemory();
                    System.out.printf("Progress: %,d/%,d records (%.1f%%) - Memory: %.1f MB%n",
                                    i, recordCount, (i * 100.0 / recordCount), usedMemory / 1024.0 / 1024.0);
                }
            }
        }

        // Final flush to ensure all data is written
        sxssfSheet.flushRows();
        System.out.printf("Generation completed: %,d records%n", recordCount);
    }

    // Pre-generate name pools for better performance
    private static String[] generateNamePool(int poolSize) {
        String[] names = new String[poolSize];
        ThreadLocalRandom random = ThreadLocalRandom.current();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        for (int i = 0; i < poolSize; i++) {
            int length = 3 + random.nextInt(6); // Names 3-8 characters
            StringBuilder name = new StringBuilder(length);

            // First character uppercase
            name.append(alphabet.charAt(random.nextInt(26)));

            // Remaining characters lowercase
            for (int j = 1; j < length; j++) {
                name.append((char) ('a' + random.nextInt(26)));
            }

            names[i] = name.toString();
        }

        return names;
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
