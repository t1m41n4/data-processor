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
  uploadError = '';
  errorMessage = '';
  uploadResult: any = null;
  maxFileSize = 100 * 1024 * 1024; // 100MB LIMIT FOR LARGE FILES

  constructor(private http: HttpClient) {
    console.log('DataUploadComponent initialized with maxFileSize:', this.maxFileSize);
  }

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
    // Validate file type
    if (!file.name.toLowerCase().endsWith('.csv')) {
      this.errorMessage = 'Please select a CSV file.';
      this.selectedFile = null;
      return;
    }

    // Validate file size (max 100MB)
    if (file.size > this.maxFileSize) {
      this.errorMessage = `File size must be less than ${this.formatFileSize(this.maxFileSize)}.`;
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
    return `${this.selectedFile.name} (${this.formatFileSize(this.selectedFile.size)})`;
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

    const formData = new FormData();
    formData.append('file', this.selectedFile);

    this.http.post<any>('http://localhost:8080/api/upload/csv', formData)
      .subscribe({
        next: (response) => {
          this.isUploading = false;
          this.uploadProgress = 100;
          this.uploadMessage = 'File uploaded successfully!';
          this.uploadResult = {
            recordsProcessed: response.totalRecords || 0,
            recordsStored: response.newRecords || 0,
            processingTimeMs: response.processingTime || 0
          };
        },
        error: (error: HttpErrorResponse) => {
          this.isUploading = false;
          this.uploadProgress = 0;
          this.errorMessage = this.getErrorMessage(error);
        }
      });
  }

  clearSelection(): void {
    this.selectedFile = null;
    this.errorMessage = '';
    this.uploadMessage = '';
    this.uploadResult = null;
    this.uploadProgress = 0;
    if (this.fileInput) {
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

  private getErrorMessage(error: HttpErrorResponse): string {
    if (error.status === 0) {
      return 'Unable to connect to server. Please check if the backend is running.';
    } else if (error.status === 400) {
      return error.error?.message || 'Invalid file format or content.';
    } else if (error.status === 413) {
      return 'File too large. Please select a smaller file.';
    } else if (error.status >= 500) {
      return 'Server error occurred. Please try again later.';
    } else {
      return `Upload failed: ${error.message}`;
    }
  }
}
