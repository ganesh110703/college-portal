
CREATE DATABASE IF NOT EXISTS college;

USE college;

CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    course VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT IGNORE INTO students (name, course, email)
VALUES
    ('Aarav Sharma', 'Computer Science', 'aarav@example.com'),
    ('Priya Reddy', 'Information Technology', 'priya@example.com'),
    ('Rahul Kumar', 'Data Science', 'rahul@example.com');
