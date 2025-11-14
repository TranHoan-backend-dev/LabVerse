# Hướng Dẫn Tạo Tài Khoản Admin

## Tài khoản Admin là gì?

**Tài khoản Admin** là user có role `ADMIN` trong hệ thống LabVerse. Với role này, bạn có thể:

- ✅ Quản lý tất cả users (xem, activate/deactivate, đổi role, xóa)
- ✅ Quản lý tất cả teams (xem, xóa)
- ✅ Xem thống kê tổng quan của hệ thống
- ✅ Truy cập Admin Dashboard tại `/admin`

## Cách Tạo Tài Khoản Admin

### ⚠️ Lưu ý quan trọng:
- **Không thể** đăng ký với role ADMIN qua form đăng ký thông thường
- Form đăng ký chỉ cho phép: `PI`, `RESEARCHER`, `STUDENT`
- Cần tạo admin user trực tiếp trong database hoặc qua API

---

## Phương Pháp 1: Tạo Qua Database (Khuyến nghị cho lần đầu)

### Bước 1: Tạo BCrypt Hash cho Password

Bạn cần hash password bằng BCrypt. Có thể dùng:

**Option A: Online BCrypt Generator**
- Truy cập: https://bcrypt-generator.com/
- Nhập password (ví dụ: `admin123`)
- Copy hash được tạo (ví dụ: `$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy`)

**Option B: Dùng Spring Boot Application**
```java
// Tạo một test class hoặc dùng Spring Shell
@Autowired
private PasswordEncoder passwordEncoder;

String hash = passwordEncoder.encode("admin123");
System.out.println(hash);
```

### Bước 2: Chạy SQL Script

1. Mở SQL Server Management Studio hoặc tool quản lý database
2. Kết nối đến database của LabVerse
3. Chạy script: `services/AccountService/src/main/resources/database/create_admin_user.sql`
4. **Nhớ thay đổi**:
   - Email: `admin@labverse.com` → email bạn muốn
   - Username: `admin` → username bạn muốn
   - Password hash: Thay bằng BCrypt hash của password bạn muốn

### Bước 3: Verify

Sau khi chạy script, kiểm tra:
```sql
SELECT u.email, u.username, r.name as role 
FROM Users u 
JOIN Role r ON u.Roleid = r.id 
WHERE r.name = 'ADMIN';
```

### Bước 4: Đăng nhập

- Email: Email bạn đã đặt trong script
- Password: Password bạn đã hash ở Bước 1

---

## Phương Pháp 2: Tạo Qua API (Nếu đã có Admin khác)

Nếu bạn đã có một admin user, bạn có thể tạo admin mới qua Admin Dashboard:

1. Đăng nhập với admin account hiện tại
2. Vào Admin Dashboard (`/admin`)
3. Tab "Users" → Tìm user cần nâng cấp
4. Click "Actions" → "Change Role"
5. Chọn role `ADMIN`

**Hoặc** dùng API trực tiếp:
```bash
PATCH /account-service/v1/api/admin/users/{userId}/role
Authorization: Bearer {admin_token}
Content-Type: application/json

{
  "roleId": "{ADMIN_ROLE_ID}"
}
```

---

## Phương Pháp 3: Tạo Admin User Đầu Tiên Qua Code (Tạm thời)

Nếu bạn muốn tự động tạo admin user khi ứng dụng khởi động, có thể thêm vào `AccountServiceApplication.java`:

```java
@PostConstruct
public void createAdminUser() {
    // Check if admin exists
    if (!userRepository.existsByEmail("admin@labverse.com")) {
        Role adminRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new RuntimeException("ADMIN role not found"));
        
        User admin = new User();
        admin.setEmail("admin@labverse.com");
        admin.setUsername("admin");
        admin.setFullName("System Administrator");
        admin.setPassword(passwordEncoder.encode("admin123")); // Change this!
        admin.setRole(adminRole);
        admin.setIsActive(true);
        
        userRepository.save(admin);
        System.out.println("Admin user created: admin@labverse.com / admin123");
    }
}
```

⚠️ **Lưu ý**: Xóa code này sau khi đã tạo admin user để tránh security risk!

---

## Kiểm Tra Role Hierarchy

Hệ thống có role hierarchy như sau:
```
ADMIN > PI > RESEARCHER > INTERN
```

Admin có quyền cao nhất và có thể:
- Quản lý tất cả users
- Xem tất cả teams (kể cả private)
- Xóa teams và users
- Xem thống kê hệ thống

---

## Troubleshooting

### Lỗi: "Role not found: ADMIN"
- **Giải pháp**: Chạy migration script `migration_add_admin_role.sql` trước

### Lỗi: "Access Denied" khi vào `/admin`
- **Giải pháp**: Kiểm tra user có role `ADMIN` không:
  ```sql
  SELECT u.email, r.name as role 
  FROM Users u 
  JOIN Role r ON u.Roleid = r.id 
  WHERE u.email = 'your-email@example.com';
  ```

### Lỗi: "Cannot change your own role"
- **Giải pháp**: Admin không thể đổi role của chính mình. Cần admin khác thực hiện.

---

## Security Best Practices

1. ✅ **Đổi password mặc định** ngay sau khi tạo admin user
2. ✅ **Sử dụng email thật** để có thể reset password nếu cần
3. ✅ **Không chia sẻ** admin credentials
4. ✅ **Tạo nhiều admin users** để tránh single point of failure
5. ✅ **Xóa code tự động tạo admin** sau khi đã setup xong

---

## Default Admin Credentials (Sau khi chạy script)

Nếu dùng script mặc định:
- **Email**: `admin@labverse.com`
- **Username**: `admin`
- **Password**: `admin123` (hoặc password bạn đã hash)

⚠️ **NHỚ ĐỔI PASSWORD NGAY SAU KHI ĐĂNG NHẬP LẦN ĐẦU!**

