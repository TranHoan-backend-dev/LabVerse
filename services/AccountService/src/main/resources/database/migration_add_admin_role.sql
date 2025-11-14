-- Migration script to add ADMIN role
-- Run this script to add ADMIN role to the database

-- Insert ADMIN role if not exists
IF NOT EXISTS (SELECT 1 FROM Role WHERE name = 'ADMIN')
BEGIN
    INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
    PRINT 'ADMIN role created successfully';
END
ELSE
BEGIN
    PRINT 'ADMIN role already exists';
END

-- Optional: Create admin user
-- Uncomment and modify the following to create an admin user
/*
DECLARE @adminRoleId UNIQUEIDENTIFIER = (SELECT id FROM Role WHERE name = 'ADMIN');
DECLARE @adminPassword NVARCHAR(255) = '$2a$10$...'; -- Replace with BCrypt hash of your password

IF NOT EXISTS (SELECT 1 FROM Users WHERE email = 'admin@labverse.com')
BEGIN
    INSERT INTO Users (id, email, username, full_name, password, created_date, updated_date, Roleid, is_active)
    VALUES (
        NEWID(),
        'admin@labverse.com',
        'admin',
        'System Administrator',
        @adminPassword, -- Replace with actual BCrypt hash
        GETDATE(),
        GETDATE(),
        @adminRoleId,
        1
    );
    PRINT 'Admin user created successfully';
END
ELSE
BEGIN
    PRINT 'Admin user already exists';
END
*/

