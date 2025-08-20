import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StudentService } from '../../services/student.service';
import { Student } from '../../models/student.model';

@Component({
  selector: 'app-student-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './student-list.component.html',
  styleUrls: ['./student-list.component.css']
})
export class StudentListComponent implements OnInit {
  students: Student[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 10;
  totalItems: number = 0;
  totalPages: number = 0;
  searchTerm: string = '';

  private studentService = inject(StudentService);

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents(): void {
    this.studentService.getStudents().subscribe((students: Student[]) => {
      this.students = students;
      this.totalItems = students.length;
      this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
    });
  }

  onPageChange(page: number): void {
    this.currentPage = page;
    this.loadStudents();
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadStudents();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadStudents();
    }
  }

  filterStudents(): void {
    if (this.searchTerm.trim() === '') {
      this.loadStudents();
    } else {
      this.studentService.getStudents().subscribe((students: Student[]) => {
        this.students = students.filter((student: Student) =>
          student.firstName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          student.lastName?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          student.email?.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
          student.className?.toLowerCase().includes(this.searchTerm.toLowerCase())
        );
        this.totalItems = this.students.length;
        this.totalPages = Math.ceil(this.totalItems / this.itemsPerPage);
      });
    }
  }
}