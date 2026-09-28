-- ComplaintBox database setup (MySQL 8+)
-- Run this file in MySQL Workbench or the mysql command line.
--
-- NOTE: This file is OPTIONAL. The application creates or updates tables
-- by itself (spring.jpa.hibernate.ddl-auto=update). It does not seed accounts.
-- The tables below match the JPA entities exactly.

CREATE DATABASE IF NOT EXISTS complaintbox_db;
USE complaintbox_db;

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(100) NOT NULL,          -- BCrypt hash
    role        VARCHAR(20)  NOT NULL,          -- USER or ADMIN
    room_number VARCHAR(10)  NOT NULL,
    created_at  DATETIME(6)  NOT NULL
);

CREATE TABLE IF NOT EXISTS complaints (
    id          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    category    VARCHAR(20)  NOT NULL,          -- PLUMBING, ELECTRICAL, CLEANING, OTHER
    title       VARCHAR(100),
    hostel_block VARCHAR(50),
    floor       VARCHAR(20),
    room_number VARCHAR(10)  NOT NULL,
    landmark    VARCHAR(100),
    description VARCHAR(500) NOT NULL,
    priority    VARCHAR(20)  NOT NULL,          -- LOW, MEDIUM, HIGH
    status      VARCHAR(20)  NOT NULL,          -- OPEN, IN_PROGRESS, RESOLVED
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT fk_complaints_user FOREIGN KEY (user_id) REFERENCES users (id)
);

-- Useful queries for the demo:
-- SELECT * FROM users;
-- SELECT * FROM complaints;
