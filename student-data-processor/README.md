# Student Data Processor

## Overview
The Student Data Processor is a full-stack application designed to manage student data efficiently. It allows users to upload CSV files containing student information, process the data, and generate reports. The application is built using Java Spring Boot for the backend, Angular for the frontend, and PostgreSQL as the database.

## Technologies Used
- **Backend**: Java Spring Boot 3.4.5
- **Frontend**: Angular 18
- **Database**: PostgreSQL
- **Build Tool**: Maven

## Features
- **Data Upload**: Users can upload CSV files containing student data.
- **Data Processing**: The application processes the uploaded data and stores it in the PostgreSQL database.
- **Reporting**: Users can generate reports based on student data, with options for pagination, searching, and filtering.
- **Student Management**: The application allows for the generation and retrieval of student records.

## Project Structure
```
student-data-processor
├── backend
│   ├── src
│   │   ├── main
│   │   │   ├── java
│   │   │   │   └── com
│   │   │   │       └── example
│   │   │   │           └── studentprocessor
│   │   │   │               ├── StudentProcessorApplication.java
│   │   │   │               ├── config
│   │   │   │               │   └── DatabaseConfig.java
│   │   │   │               ├── controller
│   │   │   │               │   ├── DataUploadController.java
│   │   │   │               │   ├── ReportController.java
│   │   │   │               │   └── StudentController.java
│   │   │   │               ├── entity
│   │   │   │               │   └── Student.java
│   │   │   │               ├── repository
│   │   │   │               │   └── StudentRepository.java
│   │   │   │               └── service
│   │   │   │                   ├── DataUploadService.java
│   │   │   │                   ├── ReportService.java
│   │   │   │                   └── StudentService.java
│   │   │   └── resources
│   │   │       ├── application.yml
│   │   │       └── db
│   │   │           └── migration
│   │   │               └── V1__Create_student_table.sql
│   │   └── test
│   │       └── java
│   │           └── com
│   │               └── example
│   │                   └── studentprocessor
│   │                       └── StudentProcessorApplicationTests.java
│   ├── pom.xml
│   └── README.md
├── frontend
│   ├── src
│   │   ├── app
│   │   │   ├── app.component.ts
│   │   │   ├── app.component.html
│   │   │   ├── app.component.css
│   │   │   ├── app.config.ts
│   │   │   ├── components
│   │   │   │   ├── data-upload
│   │   │   │   │   ├── data-upload.component.ts
│   │   │   │   │   ├── data-upload.component.html
│   │   │   │   │   └── data-upload.component.css
│   │   │   │   ├── reports
│   │   │   │   │   ├── reports.component.ts
│   │   │   │   │   ├── reports.component.html
│   │   │   │   │   └── reports.component.css
│   │   │   │   └── student-list
│   │   │   │       ├── student-list.component.ts
│   │   │   │       ├── student-list.component.html
│   │   │   │       └── student-list.component.css
│   │   │   ├── models
│   │   │   │   └── student.model.ts
│   │   │   └── services
│   │   │       ├── data-upload.service.ts
│   │   │       ├── report.service.ts
│   │   │       └── student.service.ts
│   │   ├── main.ts
│   │   ├── index.html
│   │   └── styles.css
│   ├── angular.json
│   ├── package.json
│   ├── tsconfig.json
│   └── README.md
└── README.md
```

## Getting Started
1. **Clone the repository**:
   ```
   git clone <repository-url>
   ```
2. **Set up the PostgreSQL database**:
   - Create a new database for the application.
   - Update the `application.yml` file in the backend with your database connection details.

3. **Run the backend**:
   - Navigate to the `backend` directory.
   - Use Maven to build and run the application:
   ```
   mvn spring-boot:run
   ```

4. **Run the frontend**:
   - Navigate to the `frontend` directory.
   - Install the dependencies:
   ```
   npm install
   ```
   - Start the Angular application:
   ```
   ng serve
   ```

5. **Access the application**:
   - Open your browser and go to `http://localhost:4200` to access the frontend.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## License
This project is licensed under the MIT License.