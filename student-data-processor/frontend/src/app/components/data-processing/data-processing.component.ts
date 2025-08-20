import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface ProcessingResult {
  originalFileName: string;
  csvFileName: string;
  csvFilePath: string;
  recordsProcessed: number;
  processingTime: string;
  scoreAdjustment: string;
}

@Component({
  selector: 'app-data-processing',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './data-processing.component.html',
  styleUrls: ['./data-processing.component.css']
})
export class DataProcessingComponent {
  selectedFile: File | null = null;
  isProcessing: boolean = false;

  // Result management
  successMessage: string = '';
  errorMessage: string = '';
  lastProcessingResult: ProcessingResult | null = null;

  // File validation
  acceptedFileTypes = ['.xlsx', '.xls'];
  maxFileSize = 100 * 1024 * 1024; // 100MB

  private http = inject(HttpClient);

  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    // Validate file type
    const fileExtension = file.name.toLowerCase().substring(file.name.lastIndexOf('.'));
    if (!this.acceptedFileTypes.includes(fileExtension)) {
      this.errorMessage = `Invalid file type. Please select an Excel file (${this.acceptedFileTypes.join(', ')})`;
      this.selectedFile = null;
      return;
    }

    // Validate file size
    if (file.size > this.maxFileSize) {
      this.errorMessage = `File too large. Maximum size is ${this.formatFileSize(this.maxFileSize)}`;
      this.selectedFile = null;
      return;
    }

    this.selectedFile = file;
    this.clearMessages();
  }

  onProcessFile() {
    if (!this.selectedFile) {
      this.errorMessage = 'Please select an Excel file to process';
      return;
    }

    this.isProcessing = true;
    this.clearMessages();

    const startTime = Date.now();
    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post<any>('http://localhost:8080/api/process', formData)
      .subscribe({
        next: (response: any) => {
          const endTime = Date.now();
          const duration = ((endTime - startTime) / 1000).toFixed(2);

          this.isProcessing = false;
          this.successMessage = response.message;
          this.lastProcessingResult = {
            originalFileName: this.selectedFile!.name,
            csvFileName: this.extractFileNameFromPath(response.csvFilePath),
            csvFilePath: response.csvFilePath,
            recordsProcessed: this.extractRecordCount(response.message),
            processingTime: `${duration} seconds`,
            scoreAdjustment: '+10 points added to all scores'
          };
        },
        error: (error: HttpErrorResponse) => {
          this.isProcessing = false;
          this.errorMessage = this.formatErrorMessage(error);
        }
      });
  }

  clearSelection() {
    this.selectedFile = null;
    this.clearMessages();
    // Reset file input
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
  }

  private clearMessages() {
    this.successMessage = '';
    this.errorMessage = '';
    this.lastProcessingResult = null;
  }

  private extractFileNameFromPath(filePath: string): string {
    // Extract filename from full path like "C:/var/log/applications/API/dataprocessing/filename.csv"
    const parts = filePath.split('/');
    return parts[parts.length - 1] || 'processed_data.csv';
  }

  private extractFileName(response: string): string {
    // Extract CSV filename from response like "Excel file processed successfully: processed_student_data.csv"
    const match = response.match(/processed_\w+\.csv/);
    return match ? match[0] : 'processed_data.csv';
  }

  private extractFilePath(response: string): string {
    // Since backend saves to a specific directory, construct the path
    const fileName = this.extractFileName(response);
    return `/downloads/${fileName}`;
  }

  private extractRecordCount(response: string): number {
    // Try to extract record count from response if available
    const match = response.match(/(\d+) records?/i);
    return match ? parseInt(match[1]) : 0;
  }

  private formatErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Unable to connect to the backend server. Please ensure the Spring Boot application is running on port 8080.';
    }
    if (error.status === 400) {
      return 'Invalid Excel file format. Please ensure the file contains the expected student data structure.';
    }
    if (error.status === 413) {
      return 'File too large. Please try with a smaller file.';
    }
    if (error.status === 500) {
      return 'Server error during processing. Please check the file format and try again.';
    }
    return `Error ${error.status}: ${error.error || error.message || 'Unknown error occurred'}`;
  }

  private formatFileSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  getFileInfo(): string {
    if (!this.selectedFile) return '';
    return `${this.selectedFile.name} (${this.formatFileSize(this.selectedFile.size)})`;
  }
}
