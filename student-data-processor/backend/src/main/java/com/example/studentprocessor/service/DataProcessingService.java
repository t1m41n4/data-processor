package com.example.studentprocessor.service;

import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataProcessingService {

    @Value("${app.file.csv-output-path:C:/var/log/applications/API/dataprocessing/}")
    private String csvOutputPath;

    public String convertExcelToCsv(MultipartFile file) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(csvOutputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Generate unique CSV filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String originalName = file.getOriginalFilename();
        String baseName = originalName != null ?
            originalName.substring(0, originalName.lastIndexOf('.')) : "student_data";
        String csvFileName = String.format("%s_processed_%s.csv", baseName, timestamp);
        String csvFilePath = csvOutputPath + csvFileName;

        // Read Excel file and write to CSV
        try (InputStream inputStream = file.getInputStream();
             FileWriter fileWriter = new FileWriter(csvFilePath);
             CSVWriter csvWriter = new CSVWriter(fileWriter)) {

            Workbook workbook = createWorkbook(file, inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Read first sheet

            List<String[]> csvData = new ArrayList<>();

            // Process each row
            for (Row row : sheet) {
                if (row == null) continue;

                String[] rowData = new String[6]; // studentId, firstName, lastName, DOB, class, score

                for (int i = 0; i < 6; i++) {
                    Cell cell = row.getCell(i);
                    String cellValue = getCellValueAsString(cell);

                    // Apply score adjustment (+10) for score column (index 5)
                    if (i == 5 && row.getRowNum() > 0) { // Skip header row
                        try {
                            int originalScore = Integer.parseInt(cellValue);
                            int adjustedScore = originalScore + 10;
                            rowData[i] = String.valueOf(adjustedScore);
                        } catch (NumberFormatException e) {
                            rowData[i] = cellValue; // Keep original if not a number
                        }
                    } else {
                        rowData[i] = cellValue;
                    }
                }

                csvData.add(rowData);

                // Progress logging for large datasets
                if (row.getRowNum() > 0 && row.getRowNum() % 50000 == 0) {
                    System.out.println("Processed " + row.getRowNum() + " rows...");
                }
            }

            // Write all data to CSV
            csvWriter.writeAll(csvData);
            workbook.close();
        }

        return csvFileName;
    }

    private Workbook createWorkbook(MultipartFile file, InputStream inputStream) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename != null && filename.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else {
            return new HSSFWorkbook(inputStream);
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    // Handle both integer and decimal numbers
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
