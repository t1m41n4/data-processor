#!/bin/bash

echo "========================================"
echo "Student Data Processor - Unix/Linux/Mac Setup"
echo "========================================"
echo

echo "Checking prerequisites..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "ERROR: Node.js is not installed or not in PATH"
    echo "Please install Node.js from https://nodejs.org/"
    exit 1
fi
echo "✓ Node.js found: $(node --version)"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "ERROR: Java is not installed or not in PATH"
    echo "Please install Java JDK 17+ from https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi
echo "✓ Java found"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven is not installed or not in PATH"
    echo "Please install Maven from https://maven.apache.org/download.cgi"
    exit 1
fi
echo "✓ Maven found"

# Check if Angular CLI is installed globally
if ! command -v ng &> /dev/null; then
    echo "Installing Angular CLI globally..."
    npm install -g @angular/cli
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to install Angular CLI"
        exit 1
    fi
    echo "✓ Angular CLI installed"
else
    echo "✓ Angular CLI already installed"
fi

echo
echo "Setting up backend dependencies..."
cd backend
if [ ! -d "target" ]; then
    echo "Running mvn clean install..."
    mvn clean install
    if [ $? -ne 0 ]; then
        echo "ERROR: Maven build failed"
        exit 1
    fi
    echo "✓ Backend dependencies installed"
else
    echo "✓ Backend already built"
fi

echo
echo "Setting up frontend dependencies..."
cd ../frontend
if [ ! -d "node_modules" ]; then
    echo "Running npm install..."
    npm install --legacy-peer-deps
    if [ $? -ne 0 ]; then
        echo "ERROR: npm install failed"
        exit 1
    fi
    echo "✓ Frontend dependencies installed"
else
    echo "✓ Frontend dependencies already installed"
fi

cd ..

echo
echo "========================================"
echo "Setup Complete!"
echo "========================================"
echo
echo "To start the application:"
echo
echo "1. Start backend:"
echo "   cd backend"
echo "   mvn spring-boot:run"
echo
echo "2. Start frontend (in new terminal):"
echo "   cd frontend"
echo "   npm start"
echo
echo "3. Open browser to: http://localhost:4200"
echo
echo "For detailed instructions, see DEVELOPER_SETUP.md"
echo
