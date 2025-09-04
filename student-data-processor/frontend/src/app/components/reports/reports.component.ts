import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { RouterModule } from '@angular/router';

export interface Student {
  id?: number;
  studentId: number;
  firstName: string;
  lastName: string;
  email: string;
  dob: string;
  className: string;
  score: number;
}

export interface ReportStatistics {
  totalStudents: number;
  averageScore: number;
  maxScore: number;
  minScore: number;
  passCount: number;
  failCount: number;
  passRate: number;
}

export interface ScoreDistribution {
  ranges: string[];
  counts: number[];
  colors: string[];
}

export interface SearchFilters {
  search?: string;
  minScore?: number;
  maxScore?: number;
  className?: string;
}

export interface PaginatedResponse {
  students: Student[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
  pageSize: number;
  hasNext: boolean;
  hasPrevious: boolean;
  isFirst: boolean;
  isLast: boolean;
}

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {
  // Data properties
  students: Student[] = [];
  statistics: ReportStatistics | null = null;
  scoreDistribution: ScoreDistribution | null = null;
  topPerformers: Student[] = [];

  // Search and filter properties
  searchFilters: SearchFilters = {};
  availableClasses: string[] = [];

  // Pagination properties
  currentPage = 0;
  pageSize = 10;
  totalItems = 0;
  totalPages = 0;
  hasNext = false;
  hasPrevious = false;

  // Sorting properties
  sortBy = 'studentId';
  sortDirection = 'asc';

  // UI state properties
  isLoading = false;
  isDeleting = false;
  errorMessage = '';
  successMessage = '';
  showFilters = false;

  // Export state
  isExporting = false;
  exportFormat = 'csv';

  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadInitialData();
  }

  private loadInitialData(): void {
    this.loadStudents();
    this.loadStatistics();
    this.loadScoreDistribution();
    this.loadTopPerformers();
    this.loadAvailableClasses();
  }

  loadStudents(): void {
    this.isLoading = true;
    this.errorMessage = '';

    const params = new URLSearchParams({
      page: this.currentPage.toString(),
      size: this.pageSize.toString(),
      sortBy: this.sortBy,
      sortDir: this.sortDirection
    });

    // Add search filters
    if (this.searchFilters.search) {
      params.append('search', this.searchFilters.search);
    }
    if (this.searchFilters.minScore !== undefined) {
      params.append('minScore', this.searchFilters.minScore.toString());
    }
    if (this.searchFilters.maxScore !== undefined) {
      params.append('maxScore', this.searchFilters.maxScore.toString());
    }
    if (this.searchFilters.className) {
      params.append('className', this.searchFilters.className);
    }

    this.http.get<PaginatedResponse>(`${this.apiUrl}/advanced-search?${params}`).subscribe({
      next: (response) => {
        this.students = response.students;
        this.totalItems = response.totalItems;
        this.totalPages = response.totalPages;
        this.hasNext = response.hasNext;
        this.hasPrevious = response.hasPrevious;
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = 'Failed to load students: ' + (error.error?.message || error.message);
        this.isLoading = false;
      }
    });
  }

  loadStatistics(): void {
    this.http.get<ReportStatistics>(`${this.apiUrl}/statistics`).subscribe({
      next: (stats) => {
        this.statistics = stats;
      },
      error: (error) => {
        console.error('Failed to load statistics:', error);
      }
    });
  }

  loadScoreDistribution(): void {
    this.http.get<ScoreDistribution>(`${this.apiUrl}/score-distribution`).subscribe({
      next: (distribution) => {
        this.scoreDistribution = distribution;
      },
      error: (error) => {
        console.error('Failed to load score distribution:', error);
      }
    });
  }

  loadTopPerformers(): void {
    this.http.get<Student[]>(`${this.apiUrl}/top-performers?limit=5`).subscribe({
      next: (performers) => {
        this.topPerformers = performers;
      },
      error: (error) => {
        console.error('Failed to load top performers:', error);
      }
    });
  }

  loadAvailableClasses(): void {
    this.http.get<string[]>(`${this.apiUrl}/classes`).subscribe({
      next: (classes) => {
        this.availableClasses = classes;
      },
      error: (error) => {
        console.error('Failed to load classes:', error);
      }
    });
  }

  // Search and filter methods
  onSearch(): void {
    this.currentPage = 0;
    this.loadStudents();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadStudents();
  }

  clearFilters(): void {
    this.searchFilters = {};
    this.currentPage = 0;
    this.loadStudents();
  }

  // Pagination methods
  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadStudents();
    }
  }

  goToFirstPage(): void {
    this.goToPage(0);
  }

  goToLastPage(): void {
    this.goToPage(this.totalPages - 1);
  }

  goToPreviousPage(): void {
    if (this.hasPrevious) {
      this.goToPage(this.currentPage - 1);
    }
  }

  goToNextPage(): void {
    if (this.hasNext) {
      this.goToPage(this.currentPage + 1);
    }
  }

  // Sorting methods
  sortBy_Column(column: string): void {
    if (this.sortBy === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortBy = column;
      this.sortDirection = 'asc';
    }
    this.currentPage = 0;
    this.loadStudents();
  }

  getSortIcon(column: string): string {
    if (this.sortBy !== column) return '↕️';
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  // Export methods
  exportData(format: string): void {
    this.isExporting = true;
    this.successMessage = '';
    this.errorMessage = '';

    const params = new URLSearchParams();

    // Add current filters to export
    if (this.searchFilters.search) {
      params.append('search', this.searchFilters.search);
    }
    if (this.searchFilters.minScore !== undefined) {
      params.append('minScore', this.searchFilters.minScore.toString());
    }
    if (this.searchFilters.maxScore !== undefined) {
      params.append('maxScore', this.searchFilters.maxScore.toString());
    }
    if (this.searchFilters.className) {
      params.append('className', this.searchFilters.className);
    }

    const url = `${this.apiUrl}/export/${format}?${params}`;

    // For downloads, we'll use window.open or create a download link
    if (format === 'csv') {
      this.downloadFile(url, `student_report.${format}`);
    } else {
      // For Excel and PDF, call the API and handle the response
      this.http.get(`${this.apiUrl}/export/${format}?${params}`).subscribe({
        next: (response: any) => {
          this.successMessage = `${format.toUpperCase()} report generated successfully!`;
          if (response.fileName) {
            this.successMessage += ` File: ${response.fileName}`;
          }
          this.isExporting = false;
        },
        error: (error) => {
          this.errorMessage = `Failed to export ${format.toUpperCase()}: ` + (error.error?.message || error.message);
          this.isExporting = false;
        }
      });
    }
  }

  private downloadFile(url: string, filename: string): void {
    fetch(url)
      .then(response => response.blob())
      .then(blob => {
        const downloadUrl = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = downloadUrl;
        link.download = filename;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(downloadUrl);
        this.successMessage = `CSV report downloaded successfully!`;
        this.isExporting = false;
      })
      .catch(error => {
        this.errorMessage = 'Failed to download CSV: ' + error.message;
        this.isExporting = false;
      });
  }

  // Fetch 10th Record functionality
  tenthRecord: Student | null = null;
  showTenthRecordModal = false;

  fetchTenthRecord(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    // Use the dedicated endpoint for fetching the 10th record
    const url = `${this.apiUrl}/tenth-record`;

    this.http.get<Student>(url).subscribe({
      next: (student) => {
        this.tenthRecord = student;
        this.showTenthRecordModal = true;
        this.successMessage = 'Successfully fetched the 10th record from database';
        this.isLoading = false;
      },
      error: (error) => {
        if (error.status === 404) {
          this.errorMessage = 'Database contains less than 10 records. Cannot fetch 10th record.';
        } else {
          this.errorMessage = 'Failed to fetch 10th record: ' + (error.error?.message || error.message);
        }
        this.isLoading = false;
      }
    });
  }

  closeTenthRecordModal(): void {
    this.showTenthRecordModal = false;
    this.tenthRecord = null;
  }

  // Utility methods
  getPageNumbers(): number[] {
    const pages: number[] = [];
    const start = Math.max(0, this.currentPage - 2);
    const end = Math.min(this.totalPages, start + 5);

    for (let i = start; i < end; i++) {
      pages.push(i);
    }
    return pages;
  }

  getScoreColor(score: number): string {
    if (score >= 81) return 'text-blue-600'; // Excellent
    if (score >= 61) return 'text-green-600'; // Good
    if (score >= 41) return 'text-yellow-600'; // Pass
    return 'text-red-600'; // Fail
  }

  getScoreBadge(score: number): string {
    if (score >= 81) return 'bg-blue-100 text-blue-800';
    if (score >= 61) return 'bg-green-100 text-green-800';
    if (score >= 41) return 'bg-yellow-100 text-yellow-800';
    return 'bg-red-100 text-red-800';
  }

  clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  deleteAllRecords(): void {
    // Check if there are any records to delete
    if (!this.statistics || this.statistics.totalStudents === 0) {
      this.errorMessage = '';
      this.successMessage = 'Database is already empty. No records to delete.';
      return;
    }

    // Confirm before deleting
    if (confirm(`WARNING: This action will permanently delete ALL ${this.statistics.totalStudents} records from the database. This cannot be undone. Continue?`)) {
      this.isDeleting = true;
      this.errorMessage = '';
      this.successMessage = '';

      this.http.delete(`${this.apiUrl}/clear-data`).subscribe({
        next: () => {
          this.successMessage = `Successfully deleted ${this.statistics?.totalStudents || 'all'} records from the database`;
          this.isDeleting = false;
          // Reload data to reflect the changes
          this.loadInitialData();
        },
        error: (error) => {
          this.errorMessage = 'Failed to delete records: ' + (error.error?.message || error.message);
          this.isDeleting = false;
        }
      });
    }
  }
}