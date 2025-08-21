package com.example.studentprocessor.dto;

public class ProcessingResult {
    private String csvFileName;
    private int recordsProcessed;
    private double processingTimeSeconds;

    public ProcessingResult() {}

    public ProcessingResult(String csvFileName, int recordsProcessed, double processingTimeSeconds) {
        this.csvFileName = csvFileName;
        this.recordsProcessed = recordsProcessed;
        this.processingTimeSeconds = processingTimeSeconds;
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    public int getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(int recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public double getProcessingTimeSeconds() {
        return processingTimeSeconds;
    }

    public void setProcessingTimeSeconds(double processingTimeSeconds) {
        this.processingTimeSeconds = processingTimeSeconds;
    }
}
