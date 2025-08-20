# Student Data Processor

## Overview
The Student Data Processor is a full-stack application designed to manage student data, including data generation, processing, uploading, and reporting functionalities. The application is built using Java Spring Boot for the backend and Angular for the frontend, with PostgreSQL as the database.

## Technologies Used
- **Backend**: Java Spring Boot 3.4.5
- **Frontend**: Angular 18
- **Database**: PostgreSQL

## Features
- **Data Generation**: Generate student records with fields such as student ID, first name, last name, date of birth, class, and score.
- **Data Upload**: Upload CSV files containing student data for processing.
- **Data Processing**: Process uploaded data and store it in the PostgreSQL database.
- **Reporting**: Generate reports based on student data, including pagination, searching, and filtering capabilities.

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
   cd student-data-processor
   ```

2. **Set up the PostgreSQL database**:
   - Create a new PostgreSQL database.
   - Update the `application.yml` file in the backend with your database connection details.

3. **Run the backend**:
   - Navigate to the `backend` directory.
   - Use Maven to build and run the application:
   ```
   mvn spring-boot:run
   ```

4. **Run the frontend**:
   - Navigate to the `frontend` directory.
   - Install the dependencies and start the Angular application:
   ```
   npm install
   ng serve
   ```

5. **Access the application**:
   - Open your browser and go to `http://localhost:4200` to access the frontend.

## Contributing
Contributions are welcome! Please open an issue or submit a pull request for any improvements or bug fixes.

## License
This project is licensed under the MIT License.