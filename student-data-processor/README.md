# Student Data Processor

A full-stack application for processing and managing student## 📋 **Tested Developer Experience Report**eSQL database integration.

## 🚀 Quick Start Guide - Tested Developer Experience

### ⚡ **For Developers with Prerequisites** 

**Prerequisites:** Node.js 18+, Java 17+, Maven 3.6+, PostgreSQL 17+
**Total Setup Time: 5-10 minutes**

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
# Expected: ~30 seconds, resolves all dependency conflicts
```

**Backend Setup:**
```bash
cd backend
mvn clean install
# Expected: ~60 seconds, all tests pass, build success
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
