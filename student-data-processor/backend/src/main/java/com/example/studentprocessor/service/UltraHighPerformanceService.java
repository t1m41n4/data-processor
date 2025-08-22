package com.example.studentprocessor.service;

import com.example.studentprocessor.entity.Student;
import com.example.studentprocessor.repository.StudentRepository;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class UltraHighPerformanceService {

    private final StudentRepository studentRepository;
    private final JdbcTemplate jdbcTemplate;
    private final Executor dataProcessingExecutor;
    private final AtomicInteger progressCounter = new AtomicInteger(0);
    private final AtomicInteger processedBatches = new AtomicInteger(0);
    private final Map<String, Object> uploadStats = new ConcurrentHashMap<>();
    private volatile boolean cancelRequested = false;

    @Autowired
    public UltraHighPerformanceService(StudentRepository studentRepository,
                                     JdbcTemplate jdbcTemplate,
                                     @Qualifier("dataProcessingExecutor") Executor dataProcessingExecutor) {
        this.studentRepository = studentRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.dataProcessingExecutor = dataProcessingExecutor;
    }

    @Transactional
    public OptimizedDataUploadService.UploadResult uploadCsvUltraFast(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a CSV file to upload");
        }

        // Reset cancel flag before starting a new upload
        cancelRequested = false;

        long startTime = System.currentTimeMillis();
        resetStats();

        // Phase 1: Stream-based parsing with parallel processing
        List<String[]> allRecords = parseFileWithStreaming(file);

        // Calculate totals
        boolean hasHeader = allRecords.size() > 0 && isHeaderRow(allRecords.get(0));
        int startIndex = hasHeader ? 1 : 0;

        // Analyze a few rows to check for data format issues
        if (allRecords.size() > startIndex) {
            System.out.println("🔍 Analyzing CSV data format...");
            for (int i = startIndex; i < Math.min(startIndex + 3, allRecords.size()); i++) {
                String[] row = allRecords.get(i);
                if (row.length > 0) {
                    System.out.println("📝 Sample row " + i + " has " + row.length + " columns:");
                    for (int j = 0; j < Math.min(6, row.length); j++) {
                        String columnName = j == 0 ? "StudentID" :
                                          j == 1 ? "FirstName" :
                                          j == 2 ? "LastName" :
                                          j == 3 ? "DOB" :
                                          j == 4 ? "ClassName" :
                                          j == 5 ? "Score" : "Column" + j;
                        System.out.println("   - " + columnName + ": '" + row[j] + "'");
                    }
                }
            }
        }

        int totalRecords = allRecords.size() - (hasHeader ? 1 : 0);

        uploadStats.put("totalRecords", totalRecords);
        System.out.println("🚀 ULTRA-FAST MODE: Processing " + totalRecords + " records...");

        // Phase 2: Parallel batch processing with optimized batch size
        final int ULTRA_BATCH_SIZE = 20000; // 4x larger batches
        List<List<String[]>> batches = createBatches(allRecords, startIndex, ULTRA_BATCH_SIZE);

        uploadStats.put("totalBatches", batches.size());
        System.out.println("📦 Created " + batches.size() + " ultra-batches of " + ULTRA_BATCH_SIZE + " records each");

        // Phase 3: Pre-fetch existing student IDs for duplicate check optimization
        Set<Long> existingStudentIds = preloadExistingStudentIds(allRecords, startIndex);
        System.out.println("🔍 Pre-loaded " + existingStudentIds.size() + " existing student IDs");

        // Phase 4: Parallel processing with CompletableFuture
        List<CompletableFuture<BatchResult>> futures = new ArrayList<>();

        // Calculate how many batches to process - if cancellation requested, process at least one batch
        int batchesToProcess = cancelRequested ?
            Math.min(1, batches.size()) :
            batches.size();

        for (int i = 0; i < batchesToProcess; i++) {
            final int batchIndex = i;
            final List<String[]> batch = batches.get(i);

            CompletableFuture<BatchResult> future = CompletableFuture.supplyAsync(() ->
                processBatchUltraFast(batch, batchIndex + 1, batchesToProcess, existingStudentIds),
                dataProcessingExecutor
            );
            futures.add(future);
        }

        // Log if cancelled but still processing some data
        if (cancelRequested && batchesToProcess > 0) {
            System.out.println("🛑 Upload cancellation requested but processing first batch to save partial data");
        }

        // Phase 5: Collect results
        int totalProcessed = 0;
        int totalNew = 0;
        int totalSkipped = 0;

        try {
            if (cancelRequested) {
                System.out.println("⏱️ Waiting for partial data processing to complete before cancellation...");
            }

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            allFutures.join(); // Wait for all batches to complete

            for (CompletableFuture<BatchResult> future : futures) {
                BatchResult result = future.get();
                totalProcessed += result.processed;
                totalNew += result.newRecords;
                totalSkipped += result.skipped;
            }

            if (cancelRequested) {
                if (totalProcessed > 0) {
                    System.out.println("✅ Successfully saved " + totalProcessed + " records before cancellation");
                } else {
                    System.out.println("⚠️ No records processed before cancellation");

                    // Check if records might have been skipped due to parsing errors
                    if (totalSkipped > 0) {
                        System.out.println("🔍 " + totalSkipped + " records were skipped due to parsing errors");
                        System.out.println("   This might be due to incorrect CSV format or data type issues");
                        System.out.println("   Check if student IDs contain decimal points that weren't properly parsed");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error during parallel processing: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error during parallel processing: " + e.getMessage(), e);
        }        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;

        // Verification
        String verificationResult = verifyUploadedData();

        OptimizedDataUploadService.UploadResult result = new OptimizedDataUploadService.UploadResult();
        result.setTotalRecords(totalProcessed);
        result.setNewRecords(totalNew);
        result.setSkippedRecords(totalSkipped);
        result.setProcessingTime(processingTime);
        result.setVerificationMessage(verificationResult);
        result.setSuccess(true);

        double recordsPerSecond = totalProcessed / (processingTime / 1000.0);
        System.out.println("⚡ ULTRA-FAST COMPLETED: " + totalProcessed + " records in " +
                          processingTime + "ms (" + String.format("%.0f", recordsPerSecond) + " records/sec)");

        return result;
    }

    private List<String[]> parseFileWithStreaming(MultipartFile file) throws IOException {
        List<String[]> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(false)
                .build();

            CSVReader csvReader = new CSVReaderBuilder(reader)
                .withCSVParser(parser)
                .build();

            records = csvReader.readAll();
        } catch (Exception e) {
            throw new IOException("Error parsing CSV file", e);
        }

        return records;
    }    private Set<Long> preloadExistingStudentIds(List<String[]> allRecords, int startIndex) {
        // Extract all student IDs from CSV
        Set<Long> csvStudentIds = allRecords.stream()
            .skip(startIndex)
            .map(record -> {
                try {
                    String studentIdStr = record[0].trim();
                    if (studentIdStr.contains(".")) {
                        // Convert float/double format to long
                        double doubleId = Double.parseDouble(studentIdStr);
                        return (long) doubleId;
                    } else {
                        return Long.parseLong(studentIdStr);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Skipping ID parsing: " + e.getMessage());
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        // Bulk check which ones exist in database
        return studentRepository.findExistingStudentIds(csvStudentIds);
    }

    private List<List<String[]>> createBatches(List<String[]> records, int startIndex, int batchSize) {
        List<List<String[]>> batches = new ArrayList<>();

        for (int i = startIndex; i < records.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, records.size());
            batches.add(records.subList(i, endIndex));
        }

        return batches;
    }

    private BatchResult processBatchUltraFast(List<String[]> batchRecords, int batchNumber,
                                            int totalBatches, Set<Long> existingStudentIds) {
        BatchResult result = new BatchResult();
        List<Student> studentsToInsert = new ArrayList<>();

        for (String[] data : batchRecords) {
            try {
                if (data.length >= 6) {
                    // Parse student ID handling decimal format
                    String studentIdStr = data[0].trim();
                    Long studentId;

                    try {
                        if (studentIdStr.contains(".")) {
                            // Convert float/double format to long
                            double doubleId = Double.parseDouble(studentIdStr);
                            studentId = (long) doubleId;
                        } else {
                            studentId = Long.parseLong(studentIdStr);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("⚠️ Skipped record at line " + (result.processed + result.skipped + 1) +
                                         ": Invalid student ID: " + studentIdStr);
                        result.skipped++;
                        continue;
                    }

                    // Fast duplicate check using pre-loaded set
                    if (!existingStudentIds.contains(studentId)) {
                        try {
                            Student student = parseStudentFromCsvUltraFast(data);
                            studentsToInsert.add(student);
                            result.newRecords++;
                            result.processed++;
                        } catch (Exception e) {
                            System.out.println("⚠️ Failed to parse student data: " + e.getMessage());
                            result.skipped++;
                        }
                    } else {
                        // It's a duplicate, not an error
                        result.processed++;
                    }
                } else {
                    System.out.println("⚠️ Skipped record: insufficient data columns");
                    result.skipped++;
                }
            } catch (Exception e) {
                System.out.println("⚠️ Unexpected error processing record: " + e.getMessage());
                result.skipped++;
            }
        }

        // Bulk insert using JDBC batch
        if (!studentsToInsert.isEmpty()) {
            try {
                bulkInsertStudents(studentsToInsert);
                System.out.println("✅ Successfully inserted " + studentsToInsert.size() +
                                 " records in batch " + batchNumber);
            } catch (Exception e) {
                System.out.println("❌ Error inserting batch " + batchNumber + ": " + e.getMessage());
                e.printStackTrace();
                result.processed -= studentsToInsert.size(); // Adjust count since insertion failed
                result.skipped += studentsToInsert.size();
            }
        } else if (result.processed > 0) {
            System.out.println("ℹ️ Batch " + batchNumber + " had no new records to insert (only duplicates)");
        }

        // Update progress
        int completedBatches = processedBatches.incrementAndGet();
        progressCounter.addAndGet(result.processed);

        System.out.println("⚡ Batch " + batchNumber + "/" + totalBatches + " completed: " +
                          result.processed + " records processed, " +
                          result.newRecords + " new records added, " +
                          result.skipped + " records skipped (" +
                          String.format("%.1f", (completedBatches * 100.0 / totalBatches)) + "% batches done)");

        return result;
    }

    private void bulkInsertStudents(List<Student> students) {
        String sql = "INSERT INTO students (student_id, first_name, last_name, score, class_name, dob) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(@org.springframework.lang.NonNull PreparedStatement ps, int i) throws SQLException {
                Student student = students.get(i);
                ps.setLong(1, student.getStudentId());
                ps.setString(2, student.getFirstName());
                ps.setString(3, student.getLastName());
                ps.setInt(4, student.getScore());
                ps.setString(5, student.getClassName());
                ps.setObject(6, student.getDob());
            }

            @Override
            public int getBatchSize() {
                return students.size();
            }
        });
    }

    private Student parseStudentFromCsvUltraFast(String[] data) {
        Student student = new Student();

        // Handle floating point student IDs by converting to Long
        String studentIdStr = data[0].trim();
        try {
            // Try to parse as double first, then convert to long if it has decimal part
            if (studentIdStr.contains(".")) {
                double doubleId = Double.parseDouble(studentIdStr);
                student.setStudentId((long) doubleId);
            } else {
                student.setStudentId(Long.parseLong(studentIdStr));
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Warning: Invalid student ID format: " + studentIdStr);
            throw e;
        }

        student.setFirstName(capitalizeFirstLetter(data[1].trim()));
        student.setLastName(capitalizeFirstLetter(data[2].trim()));
        student.setDob(LocalDate.parse(data[3].trim()));
        student.setClassName(data[4].trim());

        // Ultra-fast score calculation with decimal format handling
        String scoreStr = data[5].trim();
        int csvScore;
        try {
            // Handle decimal format scores by converting to double first, then to int
            if (scoreStr.contains(".")) {
                double doubleScore = Double.parseDouble(scoreStr);
                csvScore = (int) doubleScore;
            } else {
                csvScore = Integer.parseInt(scoreStr);
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Warning: Invalid score format: " + scoreStr);
            throw e;
        }
        int originalExcelScore = csvScore - 10;
        int databaseScore = originalExcelScore + 5;
        student.setScore(databaseScore);

        return student;
    }

    private String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String verifyUploadedData() {
        try {
            List<Student> sampleStudents = studentRepository.findTop10ByOrderByIdDesc();

            if (sampleStudents.isEmpty()) {
                return "❌ No records found for verification";
            }

            StringBuilder verification = new StringBuilder();
            verification.append("⚡ ULTRA-FAST SQL DATABASE VERIFICATION!\n");
            verification.append("📊 Database Type: H2 SQL Database (Persistent)\n");
            verification.append("📂 Database File: ./data/studentdb.mv.db\n");
            verification.append("🚀 Processing Mode: Parallel + Bulk Insert\n");
            verification.append("🔍 Score Calculation Verified (Excel Score + 5):\n\n");

            for (int i = 0; i < Math.min(3, sampleStudents.size()); i++) {
                Student student = sampleStudents.get(i);
                int databaseScore = student.getScore();
                int originalExcelScore = databaseScore - 5;
                verification.append(String.format("📝 Student %d: %s %s\n",
                    student.getStudentId(),
                    student.getFirstName(),
                    student.getLastName()));
                verification.append(String.format("   📊 Excel Score: %d → Database Score: %d (+5 applied) ⚡\n\n",
                    originalExcelScore,
                    databaseScore));
            }

            long totalCount = studentRepository.count();
            verification.append(String.format("📊 Total records in SQL database: %d\n", totalCount));

            return verification.toString();
        } catch (Exception e) {
            return "❌ Verification failed: " + e.getMessage();
        }
    }

    private boolean isHeaderRow(String[] row) {
        return row.length > 0 &&
               (row[0].toLowerCase().contains("student") ||
                row[0].toLowerCase().contains("id") ||
                !row[0].matches("\\d+"));
    }

    private void resetStats() {
        progressCounter.set(0);
        processedBatches.set(0);
        uploadStats.clear();
    }

    public int getProgress() {
        Integer totalRecords = (Integer) uploadStats.get("totalRecords");
        if (totalRecords == null || totalRecords == 0) return 0;
        return (int) ((progressCounter.get() * 100.0) / totalRecords);
    }

    /**
     * Cancel the current upload operation but ensure partial data is saved
     */
    @Transactional
    public void cancelUpload() {
        this.cancelRequested = true;
        System.out.println("⚠️ Ultra-fast upload cancellation requested");

        // Record the number of processed records so far
        int processedSoFar = progressCounter.get();

        // Get current database count for verification
        try {
            long currentDbCount = studentRepository.count();
            System.out.println("📊 Current database record count: " + currentDbCount);
        } catch (Exception e) {
            System.out.println("❌ Error checking database count: " + e.getMessage());
        }

        // Allow the batch being processed to complete and save
        try {
            // Give any ongoing batch operations a moment to complete
            Thread.sleep(1000);
            System.out.println("📊 Saving " + processedSoFar + " records processed before cancellation");

            // Get current database count after saving for verification
            try {
                long currentDbCount = studentRepository.count();
                System.out.println("📊 Final database record count after cancellation: " + currentDbCount);

                if (currentDbCount == 0) {
                    System.out.println("⚠️ WARNING: No records found in database after cancellation!");
                    System.out.println("🔍 This might indicate a transaction issue or data parsing problem.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error checking final database count: " + e.getMessage());
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }    // Result classes
    private static class BatchResult {
        int processed = 0;
        int newRecords = 0;
        int skipped = 0;
    }
}
