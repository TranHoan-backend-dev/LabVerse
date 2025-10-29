# Hướng dẫn chi tiết: Thiết lập Environment Variables trong IntelliJ IDEA

## Bước 1: Mở Run/Debug Configurations

### Cách 1: Từ thanh menu
1. Click vào **Run** trên thanh menu
2. Chọn **Edit Configurations...**

### Cách 2: Từ toolbar
1. Nhìn lên góc trên bên phải của IntelliJ IDEA
2. Tìm dropdown hiển thị tên application (vd: "AccountServiceApplication")
3. Click vào dropdown đó
4. Chọn **Edit Configurations...**

### Cách 3: Sử dụng phím tắt
- **Windows/Linux**: `Alt + Shift + F10` → chọn "Edit Configurations"
- **Mac**: `Ctrl + Option + R` → chọn "Edit Configurations"

---

## Bước 2: Tìm Application Configuration

Trong cửa sổ **Run/Debug Configurations**:
1. Ở panel bên trái, tìm **Application**
2. Expand (mở rộng) **Application**
3. Tìm và click vào **AccountServiceApplication**
   - Nếu chưa có, click vào dấu **+** (Add New Configuration) → chọn **Application** → đặt tên "AccountServiceApplication"

---

## Bước 3: Thêm Environment Variables

1. Trong cửa sổ configuration của **AccountServiceApplication**
2. Tìm mục **Environment variables** (thường ở giữa cửa sổ)
3. Click vào icon **📁** (folder icon) hoặc textbox bên cạnh "Environment variables:"
4. Một cửa sổ mới sẽ hiện ra: "Environment Variables"

---

## Bước 4: Nhập các biến môi trường

Trong cửa sổ **Environment Variables**, click vào dấu **+** để thêm từng biến:

### Thêm từng biến một:

#### Database Configuration
```
Name: DB_URL
Value: jdbc:sqlserver://LAPTOP-HSE5F4RV\\DGHIEU:1433;databaseName=LabVerseDB;encrypt=false
```
*(Lưu ý: Nếu báo lỗi với dấu `\\`, thử dùng một dấu `\` thôi)*

```
Name: DB_USERNAME
Value: sa
```

```
Name: DB_PASSWORD
Value: 123
```

#### JWT Configuration
```
Name: JWT_SECRET
Value: 5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
```

```
Name: JWT_EXPIRATION
Value: 86400000
```

#### Google OAuth2
```
Name: GOOGLE_CLIENT_ID
Value: 536556216607-m4aofut1aco3qb73e1tvrkijkl5a7s3k.apps.googleusercontent.com
```

#### Email Configuration
```
Name: MAIL_USERNAME
Value: hieuduong2524@gmail.com
```

```
Name: MAIL_PASSWORD
Value: qdep xamw xzwa wppx
```

---

## Bước 5: Hoặc nhập tất cả cùng lúc (Text Mode)

Thay vì thêm từng biến, bạn có thể:

1. Trong cửa sổ **Environment Variables**
2. Click vào **"Edit as text"** (icon giống văn bản, thường ở góc phải)
3. Paste toàn bộ text sau:

```
DB_URL=jdbc:sqlserver://LAPTOP-HSE5F4RV\\DGHIEU:1433;databaseName=LabVerseDB;encrypt=false;DB_USERNAME=sa;DB_PASSWORD=123;JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437;JWT_EXPIRATION=86400000;GOOGLE_CLIENT_ID=536556216607-m4aofut1aco3qb73e1tvrkijkl5a7s3k.apps.googleusercontent.com;MAIL_USERNAME=hieuduong2524@gmail.com;MAIL_PASSWORD=qdep xamw xzwa wppx
```

**Hoặc định dạng dễ đọc hơn (mỗi biến một dòng):**

```
DB_URL=jdbc:sqlserver://LAPTOP-HSE5F4RV\\DGHIEU:1433;databaseName=LabVerseDB;encrypt=false
DB_USERNAME=sa
DB_PASSWORD=123
JWT_SECRET=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
JWT_EXPIRATION=86400000
GOOGLE_CLIENT_ID=536556216607-m4aofut1aco3qb73e1tvrkijkl5a7s3k.apps.googleusercontent.com
MAIL_USERNAME=hieuduong2524@gmail.com
MAIL_PASSWORD=qdep xamw xzwa wppx
```

---

## Bước 6: Lưu Configuration

1. Click **OK** để đóng cửa sổ Environment Variables
2. Click **Apply** (nếu có)
3. Click **OK** để đóng cửa sổ Run/Debug Configurations

---

## Bước 7: Chạy Application

1. Click vào nút **Run** (▶️) trên toolbar
2. Hoặc nhấn **Shift + F10** (Windows/Linux) / **Ctrl + R** (Mac)
3. Application sẽ chạy với các environment variables đã thiết lập

---

## Kiểm tra xem đã thiết lập thành công chưa

Khi chạy application, kiểm tra console log:
- ✅ **Thành công**: Application khởi động bình thường, kết nối database thành công
- ❌ **Lỗi**: Nếu thấy lỗi kiểu:
  - `Could not resolve placeholder 'DB_PASSWORD'` → Biến chưa được set đúng
  - `Failed to configure a DataSource` → Kiểm tra lại DB_URL, DB_USERNAME, DB_PASSWORD

---

## Troubleshooting

### Lỗi: Could not resolve placeholder

**Nguyên nhân**: Environment variable chưa được set hoặc tên sai

**Giải pháp**:
1. Kiểm tra lại tên biến (phải giống CHÍNH XÁC với trong application.properties)
2. Restart IntelliJ IDEA
3. Invalidate Caches: **File** → **Invalidate Caches and Restart**

### Lỗi: Database connection failed

**Nguyên nhân**: DB_URL sai format hoặc database chưa chạy

**Giải pháp**:
1. Kiểm tra SQL Server đã chạy chưa
2. Thử dùng `\` thay vì `\\` trong DB_URL
3. Test connection trong database tool

### Environment variables không được load

**Giải pháp**:
1. Đảm bảo bạn đang run đúng configuration (AccountServiceApplication)
2. Restart IntelliJ IDEA
3. Re-build project: **Build** → **Rebuild Project**

---

## Lưu ý quan trọng

⚠️ **Environment variables này CHỈ áp dụng cho configuration hiện tại**
- Nếu bạn tạo configuration mới, bạn phải thêm lại environment variables
- Nếu đồng nghiệp clone project, họ cũng phải thiết lập lại

💡 **Mẹo**: 
- Có thể tạo file `.run/AccountServiceApplication.run.xml` để share configuration với team (nhưng KHÔNG nên commit các giá trị nhạy cảm)
- Nên document các biến cần thiết trong README để đồng nghiệp biết

---

## Bước tiếp theo

Sau khi thiết lập xong, application của bạn sẽ:
- ✅ Kết nối được với SQL Server database
- ✅ Generate JWT tokens với secret key an toàn
- ✅ Xác thực Google OAuth2 login
- ✅ Gửi email thông qua Gmail SMTP

Chúc bạn code vui vẻ! 🚀

