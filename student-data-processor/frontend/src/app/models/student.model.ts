export interface Student {
  id?: number;
  studentId: number;
  firstName: string;
  lastName: string;
  dob: string;
  className: string;
  score: number;
  email?: string;
  age?: number;
  course?: string;
  semester?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface StudentPage {
  content: Student[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data?: T;
  error?: string;
}