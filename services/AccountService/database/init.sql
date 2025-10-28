-- LabVerse Account Service Database Initialization Script
-- Database: SQL Server

-- Create Database (Run this first if database doesn't exist)
-- CREATE DATABASE LabVerseDB;
-- GO

USE LabVerseDB;
GO

-- Drop tables if they exist (for clean setup)
IF OBJECT_ID('dbo.Users', 'U') IS NOT NULL DROP TABLE dbo.Users;
IF OBJECT_ID('dbo.Role', 'U') IS NOT NULL DROP TABLE dbo.Role;
GO

-- Create Role Table
CREATE TABLE Role (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);
GO

-- Create Users Table
CREATE TABLE Users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255),
    full_name VARCHAR(255),
    avatar_url VARCHAR(255),
    password VARCHAR(255),
    google_id VARCHAR(255) UNIQUE,
    is_active BIT DEFAULT 1,
    created_date DATE DEFAULT GETDATE(),
    updated_date DATE DEFAULT GETDATE(),
    Roleid VARCHAR(36),
    FOREIGN KEY (Roleid) REFERENCES Role(id)
);
GO

-- Create Indexes for better performance
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_users_google_id ON Users(google_id);
CREATE INDEX idx_users_username ON Users(username);
GO

-- Insert Default Roles
INSERT INTO Role (id, name) VALUES 
    (NEWID(), 'PI'),
    (NEWID(), 'RESEARCHER'),
    (NEWID(), 'STUDENT');
GO

-- Verify Setup
SELECT * FROM Role;
GO

PRINT 'Database initialization completed successfully!';
GO



