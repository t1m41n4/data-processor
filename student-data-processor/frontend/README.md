# Frontend Documentation

## Student Data Processor

This project is a full-stack application designed for managing student data, including functionalities for data generation, processing, uploading, and reporting. The application is built using Angular for the frontend and Spring Boot for the backend, with PostgreSQL as the database.

### Technologies Used

- **Frontend**: Angular 18
- **Backend**: Spring Boot 3.4.5
- **Database**: PostgreSQL

### Features

- **Data Upload**: Users can upload CSV files containing student data, which will be processed and stored in the database.
- **Student Management**: The application allows for the generation, retrieval, and management of student records.
- **Reporting**: Users can generate reports based on student data, with options for pagination, searching, and filtering.

### Getting Started

1. **Clone the Repository**:
   ```
   git clone <repository-url>
   cd student-data-processor
   ```

2. **Backend Setup**:
   - Navigate to the `backend` directory.
   - Update the `application.yml` file with your PostgreSQL database credentials.
   - Run the following command to start the backend server:
     ```
     ./mvnw spring-boot:run
     ```

3. **Frontend Setup**:
   - Navigate to the `frontend` directory.
   - Install the necessary dependencies:
     ```
     npm install
     ```
   - Start the Angular application:
     ```
     ng serve
     ```

4. **Access the Application**:
   - Open your browser and go to `http://localhost:4200` to access the frontend application.

### API Endpoints

- **Upload CSV File**: `POST /api/upload`
- **Get Student Reports**: `GET /api/reports`
- **Manage Students**: `GET /api/students`, `POST /api/students`, etc.

### Contributing

Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

### License

This project is licensed under the MIT License. See the LICENSE file for more details.