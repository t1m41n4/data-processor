import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DataUploadResponse {
  success: boolean;
  message: string;
  fileName?: string;
  scoreAdjustment?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DataUploadService {
  private baseUrl = 'http://localhost:8080/api/upload';

  constructor(private http: HttpClient) { }

  uploadCsvFile(file: File): Observable<DataUploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DataUploadResponse>(`${this.baseUrl}/csv`, formData);
  }

  getUploadStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/status`);
  }
}