# Student Data Processor - Backend

## Overview

Spring Boot backend application for student data processing.

## Features

- **Data Generation**: Generate student datasets in Excel format
- **File Conversion**: Convert Excel files to CSV format
- **Database Upload**: Upload CSV data to database
- **Upload Cancellation**: Cancel uploads while preserving partial data

## Technologies Used

- **Framework**: Spring Boot 3.4.5
- **Language**: Java 17
- **Database**: PostgreSQL (H2 for development)
- **ORM**: Spring Data JPA with Hibernate
- **File Processing**: Apache POI for Excel, OpenCSV for CSV
- **Build Tool**: Maven 3.9.4