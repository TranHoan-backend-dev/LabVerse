# Troubleshooting Gateway Connection

## Kiểm tra Reading Service đã đăng ký với Eureka chưa:

1. **Mở Eureka Dashboard:**
   - http://localhost:9001
   - http://localhost:9002
   - http://localhost:9003

2. **Tìm service `READING-WORKFLOW-SERVICE`:**
   - Nếu có → Service đã đăng ký
   - Nếu không có → Service chưa đăng ký

## Các bước khắc phục:

### 1. Restart Reading Service
```bash
# Stop service hiện tại
# Rebuild và start lại
mvn clean install
# Start service
```

### 2. Kiểm tra log Reading Service
Tìm các dòng:
- `Registered APPLICATION`
- `DiscoveryClient_READING-WORKFLOW-SERVICE`
- `UP` status

### 3. Kiểm tra kết nối Eureka
- Đảm bảo ít nhất 1 Eureka Server đang chạy
- Kiểm tra `eureka.client.service-url.defaultZone` trong application.properties

### 4. Test trực tiếp Reading Service
Tìm dynamic port trong log, ví dụ: `Tomcat started on port(s): 54321`
```bash
curl http://localhost:54321/workflows
```

### 5. Test qua Gateway (sau khi service đã đăng ký)
```bash
curl http://localhost:8080/v1/api/reading-service/workflows
```

## Nếu vẫn lỗi 503:

1. Đảm bảo Reading Service đã đăng ký với Eureka
2. Đảm bảo Gateway Service có thể fetch registry từ Eureka
3. Restart cả Gateway Service và Reading Service

