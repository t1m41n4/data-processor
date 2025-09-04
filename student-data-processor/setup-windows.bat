@echo off
echo ========================================
echo Student Data Processor - Windows Setup
echo ========================================
echo.

echo Checking prerequisites...

:: Check if Node.js is installed
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Node.js is not installed or not in PATH
    echo Please install Node.js from https://nodejs.org/
    pause
    exit /b 1
)
echo ✓ Node.js found:
node --version

:: Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Java is not installed or not in PATH
    echo Please install Java JDK 17+ from https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)
echo ✓ Java found

:: Check if Maven is installed
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/download.cgi
    pause
    exit /b 1
)
echo ✓ Maven found

:: Check if Angular CLI is installed globally
ng version >nul 2>&1
if %errorlevel% neq 0 (
    echo Installing Angular CLI globally...
    npm install -g @angular/cli
    if %errorlevel% neq 0 (
        echo ERROR: Failed to install Angular CLI
        pause
        exit /b 1
    )
    echo ✓ Angular CLI installed
) else (
    echo ✓ Angular CLI already installed
)

echo.
echo Setting up backend dependencies...
cd backend
if not exist "target" (
    echo Running mvn clean install...
    mvn clean install
    if %errorlevel% neq 0 (
        echo ERROR: Maven build failed
        pause
        exit /b 1
    )
    echo ✓ Backend dependencies installed
) else (
    echo ✓ Backend already built
)

echo.
echo Setting up frontend dependencies...
cd ..\frontend
if not exist "node_modules" (
    echo Running npm install...
    npm install --legacy-peer-deps
    if %errorlevel% neq 0 (
        echo ERROR: npm install failed
        pause
        exit /b 1
    )
    echo ✓ Frontend dependencies installed
) else (
    echo ✓ Frontend dependencies already installed
)

cd ..

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo To start the application:
echo.
echo 1. Start backend:
echo    cd backend
echo    mvn spring-boot:run
echo.
echo 2. Start frontend (in new terminal):
echo    cd frontend
echo    npm start
echo.
echo 3. Open browser to: http://localhost:4200
echo.
echo For detailed instructions, see DEVELOPER_SETUP.md
echo.
pause
