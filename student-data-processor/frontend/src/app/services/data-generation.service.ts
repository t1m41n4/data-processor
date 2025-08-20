import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DataGenerationResponse {
  success: boolean;
  message: string;
  fileName?: string;
  recordCount?: number;
  filePath?: string;
  error?: string;
}

@Injectable({
  providedIn: 'root'
})
export class DataGenerationService {
  private baseUrl = 'http://localhost:8080/api/data-generation';

  constructor(private http: HttpClient) { }

  generateStudentData(recordCount: number): Observable<DataGenerationResponse> {
    const params = new HttpParams().set('recordCount', recordCount.toString());
    return this.http.post<DataGenerationResponse>(`${this.baseUrl}/generate`, null, { params });
  }

  getGenerationStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/status`);
  }
}
