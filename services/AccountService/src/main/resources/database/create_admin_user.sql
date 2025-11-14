-- Script to create ADMIN role and first admin user
-- Run this script in your SQL Server database

-- Step 1: Insert ADMIN role if not exists
IF NOT EXISTS (SELECT 1 FROM Role WHERE name = 'ADMIN')
BEGIN
    INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
    PRINT 'ADMIN role created successfully';
END
ELSE
BEGIN
    PRINT 'ADMIN role already exists';
END

-- Step 2: Create admin user
-- IMPORTANT: Replace 'YourPassword123!' with your desired password
-- You need to hash it with BCrypt first. Use an online BCrypt generator or Spring's PasswordEncoder
-- Example BCrypt hash for 'admin123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

DECLARE @adminRoleId UNIQUEIDENTIFIER = (SELECT id FROM Role WHERE name = 'ADMIN');
DECLARE @adminPassword NVARCHAR(255) = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'; -- Replace with your BCrypt hash

IF NOT EXISTS (SELECT 1 FROM Users WHERE email = 'admin@labverse.com')
BEGIN
    INSERT INTO Users (id, email, username, full_name, password, created_date, updated_date, Roleid, is_active)
    VALUES (
        NEWID(),
        'admin@labverse.com',  -- Change email if needed
        'admin',                -- Change username if needed
        'System Administrator',
        @adminPassword,         -- BCrypt hashed password
        GETDATE(),
        GETDATE(),
        @adminRoleId,
        1                       -- Active
    );
    PRINT 'Admin user created successfully';
    PRINT 'Email: admin@labverse.com';
    PRINT 'Password: admin123 (or whatever you hashed)';
END
ELSE
BEGIN
    PRINT 'Admin user already exists';
END

-- Step 3: Verify admin user was created
SELECT 
    u.id,
    u.email,
    u.username,
    u.full_name,
    r.name as role_name,
    u.is_active
FROM Users u
JOIN Role r ON u.Roleid = r.id
WHERE r.name = 'ADMIN';

