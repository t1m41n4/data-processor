import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Student, StudentPage } from '../models/student.model';

export interface ReportExportResponse {
  success: boolean;
  message: string;
  fileName?: string;
  filePath?: string;
  format?: string;
  error?: string;
}

export interface FilterParams {
  name?: string;
  email?: string;
  minAge?: number;
  maxAge?: number;
  minScore?: number;
  maxScore?: number;
  course?: string;
  semester?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private baseUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) { }

  // Original methods
  getStudentReports(
    studentId?: number,
    className?: string,
    page: number = 0,
    size: number = 10,
    sortBy: string = 'studentId',
    sortDir: string = 'asc'
  ): Observable<StudentPage> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    if (className) {
      params = params.set('className', className);
    }

    return this.http.get<StudentPage>(`${this.baseUrl}/students`, { params });
  }

  getAllClasses(): Observable<string[]> {
    return this.http.get<string[]>(`${this.baseUrl}/classes`);
  }

  // New advanced methods for reports component
  getStudentsWithFilters(filterParams: FilterParams): Observable<any> {
    let params = new HttpParams();

    // Add pagination
    if (filterParams.page !== undefined) params = params.set('page', filterParams.page.toString());
    if (filterParams.size !== undefined) params = params.set('size', filterParams.size.toString());
    if (filterParams.sortBy) params = params.set('sortBy', filterParams.sortBy);
    if (filterParams.sortDir) params = params.set('sortDir', filterParams.sortDir);

    // Add filters
    if (filterParams.name) params = params.set('name', filterParams.name);
    if (filterParams.email) params = params.set('email', filterParams.email);
    if (filterParams.minAge !== undefined) params = params.set('minAge', filterParams.minAge.toString());
    if (filterParams.maxAge !== undefined) params = params.set('maxAge', filterParams.maxAge.toString());
    if (filterParams.minScore !== undefined) params = params.set('minScore', filterParams.minScore.toString());
    if (filterParams.maxScore !== undefined) params = params.set('maxScore', filterParams.maxScore.toString());
    if (filterParams.course) params = params.set('course', filterParams.course);
    if (filterParams.semester) params = params.set('semester', filterParams.semester);

    return this.http.get<any>(`${this.baseUrl}/students/filtered`, { params });
  }

  searchStudents(searchTerm: string, paginationParams: any): Observable<any> {
    let params = new HttpParams()
      .set('search', searchTerm);

    if (paginationParams.page !== undefined) params = params.set('page', paginationParams.page.toString());
    if (paginationParams.size !== undefined) params = params.set('size', paginationParams.size.toString());
    if (paginationParams.sortBy) params = params.set('sortBy', paginationParams.sortBy);
    if (paginationParams.sortDir) params = params.set('sortDir', paginationParams.sortDir);

    return this.http.get<any>(`${this.baseUrl}/students/search`, { params });
  }

  exportStudents(exportParams: any): Observable<Blob> {
    let params = new HttpParams();

    // Add filters to export params
    Object.keys(exportParams).forEach(key => {
      if (exportParams[key] !== undefined && exportParams[key] !== null && exportParams[key] !== '') {
        params = params.set(key, exportParams[key].toString());
      }
    });

    const format = exportParams.format || 'excel';
    return this.http.get(`${this.baseUrl}/export/${format}`, {
      params,
      responseType: 'blob'
    });
  }

  // Legacy export methods (keeping for backward compatibility)
  exportToExcel(studentId?: number, className?: string): Observable<ReportExportResponse> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    if (className) {
      params = params.set('className', className);
    }
    return this.http.get<ReportExportResponse>(`${this.baseUrl}/export/excel`, { params });
  }

  exportToCsv(studentId?: number, className?: string): Observable<ReportExportResponse> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    if (className) {
      params = params.set('className', className);
    }
    return this.http.get<ReportExportResponse>(`${this.baseUrl}/export/csv`, { params });
  }

  exportToPdf(studentId?: number, className?: string): Observable<ReportExportResponse> {
    let params = new HttpParams();
    if (studentId) {
      params = params.set('studentId', studentId.toString());
    }
    if (className) {
      params = params.set('className', className);
    }
    return this.http.get<ReportExportResponse>(`${this.baseUrl}/export/pdf`, { params });
  }

  getReportStatus(): Observable<any> {
    return this.http.get(`${this.baseUrl}/status`);
  }
}