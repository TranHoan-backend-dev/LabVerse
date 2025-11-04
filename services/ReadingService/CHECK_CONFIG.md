# Kiểm tra cấu hình Reading Service

## ✅ Cấu hình hiện tại:

### 1. Gateway Routing (đúng)
```
Path: /v1/api/reading-service/**
StripPrefix: 3
URI: lb://READING-WORKFLOW-SERVICE
```
→ Request: `/v1/api/reading-service/workflows` → Service: `/workflows` ✅

### 2. Reading Service Config
- `server.port=0` (dynamic port) ✅
- `spring.application.name=READING-WORKFLOW-SERVICE` ✅
- Context path: đã bỏ (comment) ✅
- Eureka: enabled ✅

### 3. Controllers
- `/workflows` ✅
- `/notes` ✅
- `/highlights` ✅
- `/reading-lists` ✅
- `/summary` ✅

### 4. Spring Boot & Cloud Versions
- Spring Boot: `3.4.3` ✅
- Spring Cloud: `2024.0.1` ✅

## 🔍 Cần kiểm tra:

### 1. Reading Service có start thành công không?
```bash
# Xem log khi start
# Tìm các dòng:
- "Started ReadingServiceApplication"
- "Registered APPLICATION"
- "UP"
```

### 2. Service có đăng ký với Eureka không?
- Mở: http://localhost:9001
- Tìm: `READING-WORKFLOW-SERVICE`
- Status phải là: `UP`

### 3. Nếu service chưa start hoặc lỗi:
```bash
# Rebuild
mvn clean install

# Kiểm tra có lỗi compile không
# Xem log khi start
```

### 4. Test trực tiếp Reading Service (bỏ qua Gateway):
Tìm dynamic port trong log, ví dụ: `Tomcat started on port(s): 54321`
```bash
curl http://localhost:54321/workflows
```

### 5. Nếu service đã đăng ký nhưng Gateway vẫn 503:
- Restart Gateway Service
- Đợi 10-30 giây để Gateway refresh registry

## ⚠️ Lưu ý:

Nếu trước khi xóa models vẫn hoạt động, có thể:
1. Service chưa rebuild sau khi đổi Spring Boot version
2. Service chưa start lại sau khi refactor
3. Database schema có vấn đề (do Many-to-Many changes)

**Giải pháp:**
1. Rebuild: `mvn clean install`
2. Start lại Reading Service
3. Kiểm tra Eureka Dashboard
4. Test lại qua Gateway

