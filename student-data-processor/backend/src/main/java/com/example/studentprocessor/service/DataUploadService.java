package com.example.studentprocessor.service;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataUploadService {

    private final StudentRepository studentRepository;

    @Autowired
    public DataUploadService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public String uploadCsvFile(MultipartFile file) throws IOException, CsvException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a CSV file to upload");
        }

        if (!isCsvFile(file)) {
            throw new IllegalArgumentException("Please upload a valid CSV file");
        }

        List<Student> students = new ArrayList<>();
        int processedRecords = 0;
        int skippedRecords = 0;

        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> records = csvReader.readAll();

            // Skip header row if present
            boolean hasHeader = records.size() > 0 && isHeaderRow(records.get(0));
            int startIndex = hasHeader ? 1 : 0;

            for (int i = startIndex; i < records.size(); i++) {
                String[] data = records.get(i);

                try {
                    if (data.length >= 6) {
                        Student student = parseStudentFromCsv(data);
                        students.add(student);
                        processedRecords++;

                        // Batch save for performance (every 1000 records)
                        if (students.size() >= 1000) {
                            studentRepository.saveAll(students);
                            students.clear();
                            System.out.println("Saved " + processedRecords + " records...");
                        }
                    } else {
                        skippedRecords++;
                    }
                } catch (Exception e) {
                    skippedRecords++;
                    System.out.println("Skipped record at line " + (i + 1) + ": " + e.getMessage());
                }
            }

            // Save remaining records
            if (!students.isEmpty()) {
                studentRepository.saveAll(students);
            }
        }

        return String.format("Upload completed: %d records processed, %d records skipped",
                           processedRecords, skippedRecords);
    }

    private Student parseStudentFromCsv(String[] data) {
        Student student = new Student();

        // Parse studentId
        student.setStudentId(Long.parseLong(data[0].trim()));

        // Parse firstName
        student.setFirstName(data[1].trim());

        // Parse lastName
        student.setLastName(data[2].trim());

        // Parse DOB
        LocalDate dob = LocalDate.parse(data[3].trim());
        student.setDob(dob);

        // Parse className
        student.setClassName(data[4].trim());

        // Parse score and add +5 (CSV score = Excel score + 10, DB score = CSV score + 5)
        int csvScore = Integer.parseInt(data[5].trim());
        int databaseScore = csvScore + 5;
        student.setScore(databaseScore);

        return student;
    }

    private boolean isHeaderRow(String[] row) {
        // Check if first row contains header-like strings
        return row.length > 0 &&
               (row[0].toLowerCase().contains("student") ||
                row[0].toLowerCase().contains("id") ||
                !row[0].matches("\\d+"));
    }

    private boolean isCsvFile(MultipartFile file) {
        String filename = file.getOriginalFilename();
        return filename != null && filename.toLowerCase().endsWith(".csv");
    }
}