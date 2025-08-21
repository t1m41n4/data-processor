import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-data-upload',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './data-upload.component.html',
  styleUrls: ['./data-upload.component.css']
})
export class DataUploadComponent {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;
  
  selectedFile: File | null = null;
  isDragOver = false;
  isUploading = false;
  uploadProgress = 0;
  uploadMessage = '';
  errorMessage = '';
  uploadResult: any = null;
  maxFileSize = 10 * 1024 * 1024; // 10MB

  constructor(private http: HttpClient) {}

  triggerFileInput(): void {
    this.fileInput.nativeElement.click();
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectFile(input.files[0]);
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
    
    if (event.dataTransfer?.files && event.dataTransfer.files.length > 0) {
      this.selectFile(event.dataTransfer.files[0]);
    }
  }

  onFileDropped(event: DragEvent): void {
    this.onDrop(event);
  }

  private selectFile(file: File): void {
    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.errorMessage = 'Please select a CSV file.';
      this.selectedFile = null;
      return;
    }

    if (file.size > this.maxFileSize) {
      this.errorMessage = 'File size must be less than 10MB.';
      this.selectedFile = null;
      return;
    }

    this.selectedFile = file;
    this.errorMessage = '';
    this.uploadMessage = '';
    this.uploadResult = null;
  }

  getFileInfo(): string {
    if (!this.selectedFile) return '';
    return this.selectedFile.name + ' (' + this.formatFileSize(this.selectedFile.size) + ')';
  }

  onUploadFile(): void {
    this.uploadFile();
  }

  uploadFile(): void {
    if (!this.selectedFile) {
      this.errorMessage = 'Please select a file first.';
      return;
    }

    this.isUploading = true;
    this.uploadProgress = 0;
    this.errorMessage = '';
    this.uploadMessage = '';
    this.uploadResult = null;

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    const startTime = Date.now();

    this.http.post<any>('http://localhost:8080/api/upload', formData).subscribe({
      next: (response) => {
        this.isUploading = false;
        this.uploadProgress = 100;
        const endTime = Date.now();
        const processingTime = endTime - startTime;
        
        this.uploadResult = {
          recordsProcessed: response.totalRecords || 0,
          recordsStored: response.newRecords || response.totalRecords || 0,
          processingTimeMs: processingTime
        };
        
        this.uploadMessage = 'Successfully uploaded ' + this.selectedFile?.name;
        this.selectedFile = null;
        this.resetFileInput();
      },
      error: (error: HttpErrorResponse) => {
        this.isUploading = false;
        this.uploadProgress = 0;
        this.errorMessage = this.formatErrorMessage(error);
      }
    });
  }

  clearSelection(): void {
    this.removeFile();
  }

  private removeFile(): void {
    this.selectedFile = null;
    this.errorMessage = '';
    this.uploadMessage = '';
    this.uploadResult = null;
    this.resetFileInput();
  }

  private resetFileInput(): void {
    if (this.fileInput && this.fileInput.nativeElement) {
      this.fileInput.nativeElement.value = '';
    }
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  private formatErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Unable to connect to the backend server. Please ensure the Spring Boot application is running on port 8080.';
    }
    if (error.status === 400) {
      return 'Invalid CSV file format. Please ensure the file contains the expected student data structure.';
    }
    if (error.status === 413) {
      return 'File too large. Please try with a smaller file.';
    }
    if (error.status === 500) {
      return 'Server error during upload. Please check the file format and try again.';
    }
    return 'Error ' + error.status + ': ' + (error.error?.message || error.message || 'Unknown error occurred');
  }
}
