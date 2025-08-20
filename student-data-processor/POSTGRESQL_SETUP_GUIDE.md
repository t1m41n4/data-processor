# PostgreSQL Installation Guide for Student Data Processor

## Manual Installation Steps:

### Step 1: Download PostgreSQL
1. Open your web browser
2. Go to: https://www.postgresql.org/download/windows/
3. Click "Download the installer"
4. Download PostgreSQL 15.x (recommended version)

### Step 2: Installation Settings
When running the installer, use these settings to match your application.yml:

- **Installation Directory**: C:\Program Files\PostgreSQL\15
- **Data Directory**: C:\Program Files\PostgreSQL\15\data
- **Port**: 5432 (IMPORTANT - matches your Spring Boot config)
- **Superuser**: postgres
- **Password**: postgres (IMPORTANT - matches your Spring Boot config)
- **Locale**: Default (English, United States)

### Step 3: Components to Install
Select these components:
- [x] PostgreSQL Server
- [x] pgAdmin 4 (GUI tool)
- [x] Command Line Tools
- [x] Stack Builder (optional)

### Step 4: After Installation
The installer will:
1. Install PostgreSQL service
2. Start the service automatically
3. Create the postgres superuser
4. Install pgAdmin 4 for database management

### Step 5: Verify Installation
After installation, we'll run these commands to verify:
```cmd
"C:\Program Files\PostgreSQL\15\bin\psql.exe" --version
```

### Step 6: Create Student Database
We'll create the required database:
```sql
CREATE DATABASE student_db;
```

## Alternative: Portable PostgreSQL (If you prefer)
If you want a portable version without installation:
1. Go to: https://www.enterprisedb.com/download-postgresql-binaries
2. Download PostgreSQL 15 Windows x86-64 binaries
3. Extract to a folder like C:\postgresql
4. We'll configure it manually

## Your Current Configuration:
Your Spring Boot app is configured to connect to:
- Database: student_db
- Host: localhost:5432
- Username: postgres
- Password: postgres

Make sure to use these exact credentials during PostgreSQL installation!
