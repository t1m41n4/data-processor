import { Component, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface ProcessingResult {
  originalFileName: string;
  csvFileName: string;
  csvFilePath: string;
  recordsProcessed: number;
  processingTime: string;
}

@Component({
  selector: 'app-data-processing',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './data-processing.component.html',
  styleUrls: ['./data-processing.component.css'],
  encapsulation: ViewEncapsulation.None
})
export class DataProcessingComponent {
  selectedFile: File | null = null;
  isProcessing: boolean = false;
  processingResult: ProcessingResult | null = null;
  errorMessage: string = '';

  private readonly apiUrl = 'http://localhost:8080/api/process';
  private readonly maxFileSize = 100 * 1024 * 1024; // 100MB

  constructor(private http: HttpClient) {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.errorMessage = '';
      this.processingResult = null;

      // Validate file
      if (!this.isExcelFile(file)) {
        this.errorMessage = 'Please select an Excel file (.xlsx or .xls)';
        this.selectedFile = null;
        return;
      }

      if (file.size > this.maxFileSize) {
        this.errorMessage = `File size exceeds maximum limit of ${this.formatFileSize(this.maxFileSize)}`;
        this.selectedFile = null;
        return;
      }
    }
  }

  processFile(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Please select a file first';
      return;
    }

    this.isProcessing = true;
    this.errorMessage = '';
    this.processingResult = null;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    console.log('🚀 Starting Excel to CSV processing...', this.selectedFile.name);

    this.http.post<any>(this.apiUrl, formData).subscribe({
      next: (response) => {
        console.log('✅ Processing successful:', response);

        if (response.success) {
          this.processingResult = {
            originalFileName: this.selectedFile!.name,
            csvFileName: response.csvFileName,
            csvFilePath: response.csvFilePath,
            recordsProcessed: response.recordsProcessed,
            processingTime: response.processingTime
          };
        } else {
          this.errorMessage = response.message || 'Processing failed';
        }

        this.isProcessing = false;
      },
      error: (error: HttpErrorResponse) => {
        console.error('❌ Processing failed:', error);

        if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else if (error.status === 0) {
          this.errorMessage = 'Unable to connect to server. Please ensure the backend is running.';
        } else {
          this.errorMessage = `Server error: ${error.message}`;
        }

        this.isProcessing = false;
      }
    });
  }

  resetForm(): void {
    this.selectedFile = null;
    this.isProcessing = false;
    this.processingResult = null;
    this.errorMessage = '';
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';

    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  private isExcelFile(file: File): boolean {
    const fileName = file.name.toLowerCase();
    return fileName.endsWith('.xlsx') || fileName.endsWith('.xls');
  }
}
