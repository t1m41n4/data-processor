package com.example.studentprocessor.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class DataGenerationService {

    @Value("${app.file.excel-output-path:C:/var/log/applications/API/dataprocessing/}")
    private String excelOutputPath;

    private static final String[] CLASS_OPTIONS = {"Class1", "Class2", "Class3", "Class4", "Class5"};
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random random = new Random();

    public String generateStudentExcelFile(int recordCount) throws IOException {
        // Create output directory if it doesn't exist
        Path outputDir = Paths.get(excelOutputPath);
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // Generate unique filename with timestamp
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = String.format("student_data_%s_%d_records.xlsx", timestamp, recordCount);
        String fullPath = excelOutputPath + fileName;

        // Create workbook and sheet
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            // Create header row
            createHeaderRow(sheet);

            // Generate student data rows
            generateStudentDataRows(sheet, recordCount);

            // Auto-size columns
            autoSizeColumns(sheet);

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(fullPath)) {
                workbook.write(fileOut);
            }
        }

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

    private void generateStudentDataRows(Sheet sheet, int recordCount) {
        for (int i = 1; i <= recordCount; i++) {
            Row row = sheet.createRow(i);

            // studentId - numeric incremental starting from 1
            row.createCell(0).setCellValue(i);

            // firstName - random string 3-8 characters
            row.createCell(1).setCellValue(generateRandomName(3, 8));

            // lastName - random string 3-8 characters
            row.createCell(2).setCellValue(generateRandomName(3, 8));

            // DOB - random date between 2000-01-01 and 2010-12-31
            row.createCell(3).setCellValue(generateRandomDateOfBirth().toString());

            // class - random from predefined options
            row.createCell(4).setCellValue(getRandomClass());

            // score - random number between 55 and 75
            row.createCell(5).setCellValue(generateRandomScore());

            // Progress logging for large datasets
            if (i % 50000 == 0) {
                System.out.println("Generated " + i + " records...");
            }
        }
    }

    private String generateRandomName(int minLength, int maxLength) {
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder name = new StringBuilder();

        // First character uppercase
        name.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));

        // Remaining characters lowercase
        for (int i = 1; i < length; i++) {
            name.append(ALPHABET.toLowerCase().charAt(random.nextInt(ALPHABET.length())));
        }

        return name.toString();
    }

    private LocalDate generateRandomDateOfBirth() {
        // Generate random date between 2000-01-01 and 2010-12-31
        LocalDate startDate = LocalDate.of(2000, 1, 1);
        LocalDate endDate = LocalDate.of(2010, 12, 31);

        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        long randomEpochDay = startEpochDay + random.nextLong(endEpochDay - startEpochDay + 1);

        return LocalDate.ofEpochDay(randomEpochDay);
    }

    private String getRandomClass() {
        return CLASS_OPTIONS[random.nextInt(CLASS_OPTIONS.length)];
    }

    private int generateRandomScore() {
        // Generate random score between 55 and 75 (inclusive)
        return random.nextInt(21) + 55; // 21 possible values: 55-75
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}
