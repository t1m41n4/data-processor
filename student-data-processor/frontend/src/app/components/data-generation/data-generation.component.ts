import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface GenerationResult {
  recordCount: number;
  fileName: string;
  fileSize: string;
  generationTime: string;
}

@Component({
  selector: 'app-data-generation',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './data-generation.component.html',
  styleUrls: ['./data-generation.component.css']
})
export class DataGenerationComponent {
  recordCount: number = 1000000;
  loading: boolean = false;

  // Result management
  successMessage: string = '';
  errorMessage: string = '';
  lastResult: GenerationResult | null = null;

  private http = inject(HttpClient);

  generateData() {
    if (this.recordCount <= 0) {
      this.errorMessage = 'Please enter a valid number greater than 0';
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';
    this.lastResult = null;

    const startTime = Date.now();

    this.http.post(`http://localhost:8080/api/generate?recordCount=${this.recordCount}`, {}, {responseType: 'text'})
      .subscribe({
        next: (response: string) => {
          const endTime = Date.now();
          const duration = ((endTime - startTime) / 1000).toFixed(2);

          this.successMessage = response;
          this.lastResult = {
            recordCount: this.recordCount,
            fileName: this.extractFileName(response),
            fileSize: this.estimateFileSize(this.recordCount),
            generationTime: `${duration} seconds`
          };

          this.loading = false;
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = this.formatErrorMessage(error);
          this.loading = false;
        }
      });
  }

  clearResults() {
    this.successMessage = '';
    this.errorMessage = '';
    this.lastResult = null;
  }

  private extractFileName(response: string): string {
    // Extract filename from response like "Excel file generated successfully: student_data_20250820_1000000_records.xlsx"
    const match = response.match(/student_data_\d+_\d+_records\.xlsx/);
    return match ? match[0] : 'student_data.xlsx';
  }

  private estimateFileSize(recordCount: number): string {
    // Rough estimation: ~100 bytes per record in Excel format
    const bytes = recordCount * 100;
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(1)} GB`;
  }

  private formatErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Unable to connect to the backend server. Please ensure the Spring Boot application is running on port 8080.';
    }
    if (error.status === 400) {
      return 'Invalid request. Please check the record count is within valid limits.';
    }
    if (error.status === 500) {
      return 'Server error occurred during file generation. Please try with a smaller record count.';
    }
    return `Error ${error.status}: ${error.error || error.message || 'Unknown error occurred'}`;
  }
}
