# Hướng Dẫn Test Admin Endpoints Qua Postman

## 📋 Mục Lục
1. [Chuẩn Bị](#chuẩn-bị)
2. [Lấy JWT Token](#lấy-jwt-token)
3. [Test User Management Endpoints](#test-user-management-endpoints)
4. [Test Statistics Endpoints](#test-statistics-endpoints)
5. [Troubleshooting](#troubleshooting)

---

## 🔧 Chuẩn Bị

### 1. Base URL
```
http://localhost:8080/account-service
```

### 2. Tạo Admin User
Trước khi test, đảm bảo bạn đã có admin user:
- Chạy script: `services/AccountService/src/main/resources/database/create_admin_user.sql`
- Hoặc đăng ký user và update role thành ADMIN trong database

### 3. Collection Variables trong Postman
Tạo các variables trong Postman Collection:
- `base_url`: `http://localhost:8080/account-service`
- `token`: (sẽ được set sau khi login)
- `admin_user_id`: (sẽ được set sau khi lấy user details)

---

## 🔑 Lấy JWT Token

### Step 1: Login với Admin Account

**Request:**
```
POST {{base_url}}/v1/api/auth/login
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "admin@labverse.com",
  "password": "admin123"
}
```

**Response:**
```json
{
  "status": 200,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": "encoded-user-id",
    "email": "admin@labverse.com",
    "username": "admin",
    "fullName": "System Administrator",
    "role": "ADMIN"
  }
}
```

### Step 2: Copy Token
- Copy giá trị `token` từ response
- Set vào variable `token` trong Postman
- Hoặc dùng Postman script để tự động set:

```javascript
// Postman Test Script (sau khi login)
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
    pm.environment.set("admin_user_id", jsonData.data.userId);
    console.log("Token saved:", jsonData.data.token);
}
```

---

## 👥 Test User Management Endpoints

### 1. Get All Users

**Request:**
```
GET {{base_url}}/admin/users?page=0&size=20
Authorization: Bearer {{token}}
```

**Query Parameters:**
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)
- `search` (optional): Search by email, username, or fullName
- `role` (optional): Filter by role (ADMIN, PI, RESEARCHER, STUDENT)
- `isActive` (optional): Filter by status (true/false)

**Example với filters:**
```
GET {{base_url}}/admin/users?page=0&size=20&search=admin&role=ADMIN&isActive=true
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "encoded-id",
        "email": "admin@labverse.com",
        "username": "admin",
        "fullName": "System Administrator",
        "avatarUrl": null,
        "role": "ADMIN",
        "createdDate": "2024-01-01",
        "updatedDate": "2024-01-01"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "currentPage": 0,
    "size": 20
  }
}
```

**Postman Test Script:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Response has users data", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data).to.have.property('content');
    pm.expect(jsonData.data.content).to.be.an('array');
});
```

---

### 2. Get User Details

**Request:**
```
GET {{base_url}}/admin/users/{{user_id}}
Authorization: Bearer {{token}}
```

**Path Variable:**
- `user_id`: Encoded user ID (lấy từ response của Get All Users)

**Expected Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "encoded-id",
    "email": "user@example.com",
    "username": "username",
    "fullName": "Full Name",
    "avatarUrl": null,
    "role": "RESEARCHER",
    "isActive": true,
    "createdDate": "2024-01-01",
    "updatedDate": "2024-01-01",
    "paperCount": 0,
    "teamCount": 2,
    "collectionCount": 5
  }
}
```

---

### 3. Activate User

**Request:**
```
PATCH {{base_url}}/admin/users/{{user_id}}/activate
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "User activated successfully",
  "data": null
}
```

---

### 4. Deactivate User

**Request:**
```
PATCH {{base_url}}/admin/users/{{user_id}}/deactivate
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "User deactivated successfully",
  "data": null
}
```

---

### 5. Change User Role

**Request:**
```
PATCH {{base_url}}/admin/users/{{user_id}}/role
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "roleId": "role-uuid-here"
}
```

**Lưu ý:** 
- Cần lấy `roleId` từ database (Role table)
- Không thể đổi role của chính mình

**Expected Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "encoded-id",
    "email": "user@example.com",
    "role": "PI"
  }
}
```

**Cách lấy Role ID:**
```sql
SELECT id, name FROM Role;
```

---

### 6. Delete User

**Request:**
```
DELETE {{base_url}}/admin/users/{{user_id}}
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "User deleted successfully",
  "data": null
}
```

**Lưu ý:** 
- Không thể delete chính mình
- Delete là soft delete (set isActive = false)

---

## 📊 Test Statistics Endpoints

### Get Overview Statistics

**Request:**
```
GET {{base_url}}/admin/statistics/overview
Authorization: Bearer {{token}}
```

**Expected Response:**
```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "totalUsers": 10,
    "activeUsers": 8,
    "inactiveUsers": 2,
    "totalPapers": 0,
    "papersThisMonth": 0,
    "totalCollections": 0
  }
}
```

---

## 🔍 Troubleshooting

### Lỗi 401 Unauthorized

**Nguyên nhân:**
- Token không hợp lệ hoặc đã hết hạn
- Thiếu header Authorization

**Giải pháp:**
1. Login lại để lấy token mới
2. Kiểm tra header: `Authorization: Bearer {{token}}`
3. Đảm bảo token không có khoảng trắng thừa

---

### Lỗi 403 Forbidden

**Nguyên nhân:**
- User không có role ADMIN
- Role không được set đúng trong database

**Giải pháp:**
1. Kiểm tra role trong database:
```sql
SELECT u.email, r.name as role 
FROM Users u 
JOIN Role r ON u.Roleid = r.id 
WHERE u.email = 'admin@labverse.com';
```

2. Đảm bảo role là `ADMIN` (không phải `ROLE_ADMIN`)

3. Kiểm tra SecurityConfig có đúng không:
```java
hierarchy.setHierarchy("ROLE_ADMIN > ROLE_PI > ROLE_RESEARCHER > ROLE_INTERN");
```

---

### Lỗi 500 Internal Server Error

**Nguyên nhân phổ biến:**

#### 1. Query Error - NULL handling
**Lỗi:** `NullPointerException` hoặc `QueryException`

**Giải pháp:**
- Đã fix trong code: Query đã check `IS NULL OR = ''`
- Service đã normalize empty strings thành null

**Test query trực tiếp:**
```sql
-- Test query với NULL
SELECT u.* FROM Users u 
WHERE (NULL IS NULL OR LOWER(u.email) LIKE LOWER('%' + NULL + '%'))
AND (NULL IS NULL OR u.role.name = NULL)
AND (NULL IS NULL OR u.isActive = NULL);

-- Test query với empty string
SELECT u.* FROM Users u 
WHERE ('' IS NULL OR '' = '' OR LOWER(u.email) LIKE LOWER('%' + '' + '%'))
AND ('' IS NULL OR '' = '' OR u.role.name = '')
AND (NULL IS NULL OR u.isActive = NULL);
```

#### 2. Role không tồn tại
**Lỗi:** `ResourceNotFoundException: Role not found`

**Giải pháp:**
```sql
-- Kiểm tra roles có trong database
SELECT * FROM Role;

-- Nếu thiếu ADMIN role:
INSERT INTO Role (id, name) VALUES (NEWID(), 'ADMIN');
```

#### 3. User không có Role
**Lỗi:** `NullPointerException` khi access `user.getRole().getName()`

**Giải pháp:**
```sql
-- Kiểm tra users không có role
SELECT u.* FROM Users u WHERE u.Roleid IS NULL;

-- Fix: Gán role cho user
UPDATE Users 
SET Roleid = (SELECT id FROM Role WHERE name = 'RESEARCHER')
WHERE Roleid IS NULL;
```

#### 4. LazyInitializationException
**Lỗi:** `could not initialize proxy - no Session`

**Giải pháp:**
- Đảm bảo `@Transactional` được dùng đúng
- Check `fetch = FetchType.EAGER` cho relationship cần thiết

---

### Lỗi 400 Bad Request

#### Invalid User ID Format
**Nguyên nhân:** ID không phải encoded format

**Giải pháp:**
- Sử dụng encoded ID từ response của Get All Users
- Không dùng raw UUID

#### Cannot change your own role
**Nguyên nhân:** Admin đang cố đổi role của chính mình

**Giải pháp:**
- Dùng user ID khác để test
- Hoặc dùng admin khác để đổi role

---

## 📝 Postman Collection Template

### Collection Structure:
```
LabVerse Admin API
├── Authentication
│   └── Login Admin
├── User Management
│   ├── Get All Users
│   ├── Get User Details
│   ├── Activate User
│   ├── Deactivate User
│   ├── Change User Role
│   └── Delete User
└── Statistics
    └── Get Overview Statistics
```

### Pre-request Script (cho tất cả requests):
```javascript
// Auto-set token if available
if (pm.environment.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("token")
    });
}
```

### Test Script (cho tất cả requests):
```javascript
// Check if response is successful
if (pm.response.code >= 200 && pm.response.code < 300) {
    console.log("✅ Request successful");
} else {
    console.log("❌ Request failed:", pm.response.text());
}
```

---

## 🧪 Test Cases

### Test Case 1: Get All Users (No Filters)
```
GET /admin/users?page=0&size=20
Expected: 200 OK, returns paginated users
```

### Test Case 2: Get All Users (With Search)
```
GET /admin/users?page=0&size=20&search=admin
Expected: 200 OK, returns filtered users
```

### Test Case 3: Get All Users (With Role Filter)
```
GET /admin/users?page=0&size=20&role=ADMIN
Expected: 200 OK, returns only ADMIN users
```

### Test Case 4: Get All Users (With Status Filter)
```
GET /admin/users?page=0&size=20&isActive=true
Expected: 200 OK, returns only active users
```

### Test Case 5: Activate User
```
PATCH /admin/users/{id}/activate
Expected: 200 OK, user.isActive = true
```

### Test Case 6: Deactivate User
```
PATCH /admin/users/{id}/deactivate
Expected: 200 OK, user.isActive = false
```

### Test Case 7: Get Statistics
```
GET /admin/statistics/overview
Expected: 200 OK, returns statistics object
```

---

## 🔧 Debug Backend

### 1. Check Logs
Xem logs trong console để tìm lỗi cụ thể:
```
2025-11-14T12:34:11.266+07:00 ERROR ... Exception: ...
```

### 2. Test Query Trực Tiếp
```sql
-- Test query findAllWithFilters
DECLARE @search NVARCHAR(255) = NULL;
DECLARE @role NVARCHAR(255) = NULL;
DECLARE @isActive BIT = NULL;

SELECT u.* 
FROM Users u
WHERE (@search IS NULL OR @search = '' OR LOWER(u.email) LIKE LOWER('%' + @search + '%'))
AND (@role IS NULL OR @role = '' OR EXISTS (
    SELECT 1 FROM Role r WHERE r.id = u.Roleid AND r.name = @role
))
AND (@isActive IS NULL OR u.isActive = @isActive);
```

### 3. Check Database State
```sql
-- Check users và roles
SELECT 
    u.id,
    u.email,
    u.username,
    u.isActive,
    r.name as role_name
FROM Users u
LEFT JOIN Role r ON u.Roleid = r.id;

-- Check ADMIN role exists
SELECT * FROM Role WHERE name = 'ADMIN';

-- Check admin users
SELECT u.*, r.name as role
FROM Users u
JOIN Role r ON u.Roleid = r.id
WHERE r.name = 'ADMIN';
```

### 4. Enable SQL Logging
Thêm vào `application.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
```

---

## ✅ Checklist Trước Khi Test

- [ ] Admin user đã được tạo trong database
- [ ] ADMIN role đã tồn tại trong Role table
- [ ] User đã được gán ADMIN role
- [ ] Backend service đang chạy (port 8080)
- [ ] Database connection OK
- [ ] JWT token đã được lấy và set vào Postman
- [ ] Authorization header đã được thêm vào requests

---

## 📞 Common Issues & Solutions

### Issue 1: "No static resource v1/api/v1/api/admin/users"
**Nguyên nhân:** URL bị duplicate prefix  
**Giải pháp:** Đã fix - URL giờ là `/admin/users` không còn `/v1/api/admin/users`

### Issue 2: "Access Denied" hoặc 403
**Nguyên nhân:** Role không đúng  
**Giải pháp:** 
```sql
-- Verify role
SELECT u.email, r.name 
FROM Users u 
JOIN Role r ON u.Roleid = r.id 
WHERE u.email = 'your-admin-email@example.com';
```

### Issue 3: Query returns empty result
**Nguyên nhân:** Query conditions quá strict  
**Giải pháp:** Test với NULL values trước:
```
GET /admin/users?page=0&size=20
```

---

## 🎯 Quick Test Commands

### Test với cURL:
```bash
# Login
curl -X POST http://localhost:8080/account-service/v1/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@labverse.com","password":"admin123"}'

# Get Users (thay TOKEN bằng token từ login)
curl -X GET "http://localhost:8080/account-service/admin/users?page=0&size=20" \
  -H "Authorization: Bearer TOKEN"

# Get Statistics
curl -X GET "http://localhost:8080/account-service/admin/statistics/overview" \
  -H "Authorization: Bearer TOKEN"
```

---

Chúc bạn test thành công! 🚀

