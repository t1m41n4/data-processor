CREATE TABLE student (
    student_id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE NOT NULL,
    class VARCHAR(20) NOT NULL,
    score DECIMAL(5, 2) NOT NULL
);