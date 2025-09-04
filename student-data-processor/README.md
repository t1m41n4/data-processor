# Student Data Processor

A full-stack application for processing and managing student data

## 🚀 Quick Start Guide


**Prerequisites:** Angular 18 + Java 17+, Maven 3.6+, PostgreSQL 17+

#### **Step 1: Clone and Navigate**
```bash
git clone <repository-url>
cd student-data-processor
```

#### **Step 2: Install Dependencies**

**Frontend Setup:**
```bash
cd frontend
npm install --legacy-peer-deps
```

**Backend Setup:**
```bash
cd backend

# Add Maven to PATH  
$env:PATH += ";C:\tools\apache-maven-3.9.4\bin"

mvn clean install
```

#### **Step 3: Start the Application**

**Terminal 1 - Backend:**
```bash
cd backend
# Add Maven to PATH for this session
$env:PATH += ";C:\tools\apache-maven-3.9.4\bin"

# Verify Maven is now available
mvn --version

# Now run the backend
mvn spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
ng serve --port 4200
# Expected output: "Angular Live Development Server is listening on port 4200"
```



