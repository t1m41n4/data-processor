import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DataProcessingResponse {
  success: boolean;
  message: string;
  csvFileName?: string;
  originalFileName?: string;
  csvFilePath?: string;
  format?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DataProcessingService {
  private baseUrl = 'http://localhost:8080/api/data-processing';

  constructor(private http: HttpClient) { }

  convertExcelToCsv(file: File): Observable<DataProcessingResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<DataProcessingResponse>(`${this.baseUrl}/excel-to-csv`, formData);
  }

  getProcessingStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/status`);
  }
}
