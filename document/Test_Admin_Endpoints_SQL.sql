-- ============================================
-- SQL Script để Test và Debug Admin Backend
-- ============================================

-- 1. Kiểm tra Roles
PRINT '=== CHECKING ROLES ===';
SELECT id, name FROM Role;
GO

-- 2. Kiểm tra Admin User
PRINT '=== CHECKING ADMIN USER ===';
SELECT 
    u.id,
    u.email,
    u.username,
    u.full_name,
    u.is_active,
    r.id as role_id,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id
WHERE r.name = 'ADMIN' OR u.email = 'admin@labverse.com';
GO

-- 3. Kiểm tra Users không có Role (sẽ gây lỗi)
PRINT '=== CHECKING USERS WITHOUT ROLE ===';
SELECT 
    u.id,
    u.email,
    u.username,
    u.Roleid
FROM Users u
WHERE u.Roleid IS NULL;
GO

-- 4. Fix Users không có Role (nếu có)
-- UNCOMMENT để chạy:
/*
UPDATE Users 
SET Roleid = (SELECT TOP 1 id FROM Role WHERE name = 'RESEARCHER')
WHERE Roleid IS NULL;
PRINT 'Fixed users without role';
GO
*/

-- 5. Test Query findAllWithFilters với NULL
PRINT '=== TESTING QUERY WITH NULL VALUES ===';
DECLARE @search NVARCHAR(255) = NULL;
DECLARE @role NVARCHAR(255) = NULL;
DECLARE @isActive BIT = NULL;

SELECT 
    u.id,
    u.email,
    u.username,
    u.full_name,
    u.is_active,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id
WHERE (@search IS NULL OR @search = '' OR 
       LOWER(u.email) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.username) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.full_name) LIKE LOWER('%' + @search + '%'))
AND (@role IS NULL OR @role = '' OR r.name = @role)
AND (@isActive IS NULL OR u.is_active = @isActive);
GO

-- 6. Test Query với Search
PRINT '=== TESTING QUERY WITH SEARCH ===';
DECLARE @search NVARCHAR(255) = 'admin';
DECLARE @role NVARCHAR(255) = NULL;
DECLARE @isActive BIT = NULL;

SELECT 
    u.id,
    u.email,
    u.username,
    u.full_name,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id
WHERE (@search IS NULL OR @search = '' OR 
       LOWER(u.email) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.username) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.full_name) LIKE LOWER('%' + @search + '%'))
AND (@role IS NULL OR @role = '' OR r.name = @role)
AND (@isActive IS NULL OR u.is_active = @isActive);
GO

-- 7. Test Query với Role Filter
PRINT '=== TESTING QUERY WITH ROLE FILTER ===';
DECLARE @search NVARCHAR(255) = NULL;
DECLARE @role NVARCHAR(255) = 'ADMIN';
DECLARE @isActive BIT = NULL;

SELECT 
    u.id,
    u.email,
    u.username,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id
WHERE (@search IS NULL OR @search = '' OR 
       LOWER(u.email) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.username) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.full_name) LIKE LOWER('%' + @search + '%'))
AND (@role IS NULL OR @role = '' OR r.name = @role)
AND (@isActive IS NULL OR u.is_active = @isActive);
GO

-- 8. Test Statistics Queries
PRINT '=== TESTING STATISTICS QUERIES ===';

-- Total Users
SELECT COUNT(*) as total_users FROM Users;
GO

-- Active Users
SELECT COUNT(*) as active_users 
FROM Users 
WHERE is_active = 1;
GO

-- Inactive Users
SELECT COUNT(*) as inactive_users 
FROM Users 
WHERE is_active = 0 OR is_active IS NULL;
GO

-- 9. Kiểm tra Role ID để dùng trong Change Role API
PRINT '=== ROLE IDs FOR CHANGE ROLE API ===';
SELECT id, name FROM Role;
GO

-- 10. Verify Admin User có thể login
PRINT '=== VERIFY ADMIN USER CAN LOGIN ===';
SELECT 
    u.email,
    u.password IS NOT NULL as has_password,
    u.is_active,
    r.name as role
FROM Users u
JOIN Role r ON u.Roleid = r.id
WHERE u.email = 'admin@labverse.com' AND r.name = 'ADMIN';
GO

