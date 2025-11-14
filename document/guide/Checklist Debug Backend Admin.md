# Checklist Debug Backend Admin

## 🔍 Kiểm Tra Lỗi 500

### Step 1: Kiểm Tra Database Connection
```sql
-- Test connection
SELECT @@VERSION;
```

### Step 2: Kiểm Tra Role Table
```sql
-- Xem tất cả roles
SELECT * FROM Role;

-- Nếu thiếu ADMIN role:
INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
```

### Step 3: Kiểm Tra Admin User
```sql
-- Xem users và roles
SELECT 
    u.id,
    u.email,
    u.username,
    u.isActive,
    r.id as role_id,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id;

-- Kiểm tra admin user cụ thể
SELECT u.*, r.name as role
FROM Users u
JOIN Role r ON u.Roleid = r.id
WHERE u.email = 'admin@labverse.com';
```

### Step 4: Kiểm Tra Users Không Có Role
```sql
-- Users không có role sẽ gây lỗi
SELECT u.* 
FROM Users u 
WHERE u.Roleid IS NULL;

-- Fix: Gán role mặc định
UPDATE Users 
SET Roleid = (SELECT TOP 1 id FROM Role WHERE name = 'RESEARCHER')
WHERE Roleid IS NULL;
```

### Step 5: Test Query Trực Tiếp
```sql
-- Test query với NULL
DECLARE @search NVARCHAR(255) = NULL;
DECLARE @role NVARCHAR(255) = NULL;
DECLARE @isActive BIT = NULL;

SELECT u.* 
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id
WHERE (@search IS NULL OR @search = '' OR 
       LOWER(u.email) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.username) LIKE LOWER('%' + @search + '%') OR
       LOWER(u.full_name) LIKE LOWER('%' + @search + '%'))
AND (@role IS NULL OR @role = '' OR r.name = @role)
AND (@isActive IS NULL OR u.is_active = @isActive);
```

### Step 6: Kiểm Tra Application Logs
Xem logs trong console để tìm stack trace cụ thể:
```
ERROR ... Exception: ...
at com.se1853_jv.service.impl.AdminServiceImpl.getAllUsers(...)
```

---

## 🛠️ Common Fixes

### Fix 1: Query Syntax Error
Nếu lỗi về CONCAT, có thể SQL Server version không support:
```java
// Thay CONCAT bằng + operator (SQL Server)
LOWER(u.email) LIKE LOWER('%' + :search + '%')
```

### Fix 2: NullPointerException
Đảm bảo tất cả users đều có role:
```sql
-- Check và fix
UPDATE Users 
SET Roleid = (SELECT TOP 1 id FROM Role WHERE name = 'RESEARCHER')
WHERE Roleid IS NULL;
```

### Fix 3: LazyInitializationException
Role đã có `FetchType.EAGER` nên không có vấn đề này.

### Fix 4: Transaction Issue
Đảm bảo `@Transactional` được dùng đúng:
- `getAllUsers()` không cần `@Transactional` (read-only)
- Các methods modify cần `@Transactional`

---

## 📋 Test Checklist

- [ ] Database connection OK
- [ ] ADMIN role exists
- [ ] Admin user exists và có ADMIN role
- [ ] Tất cả users đều có role (không có NULL)
- [ ] Query test trực tiếp trong SQL Server OK
- [ ] Backend service đang chạy
- [ ] JWT token hợp lệ
- [ ] Authorization header đúng format

---

## 🧪 Quick Test SQL

```sql
-- 1. Check roles
SELECT * FROM Role;

-- 2. Check admin user
SELECT u.email, r.name 
FROM Users u 
JOIN Role r ON u.Roleid = r.id 
WHERE r.name = 'ADMIN';

-- 3. Check users without role
SELECT COUNT(*) as users_without_role 
FROM Users 
WHERE Roleid IS NULL;

-- 4. Test query
SELECT COUNT(*) 
FROM Users u
JOIN Role r ON u.Roleid = r.id
WHERE (NULL IS NULL OR NULL = '' OR 1=1)
AND (NULL IS NULL OR NULL = '' OR 1=1)
AND (NULL IS NULL OR u.is_active = NULL);
```

