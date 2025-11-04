# Kiểm tra ReadingService đăng ký với Eureka

## Các bước kiểm tra:

### 1. Kiểm tra ReadingService có đang chạy không
- Mở terminal/log của ReadingService
- Tìm log: "Started ReadingServiceApplication" hoặc tương tự
- Kiểm tra có lỗi nào không (database connection, etc.)

### 2. Kiểm tra Eureka Dashboard
- Mở browser và truy cập một trong các Eureka server:
  - http://localhost:9001
  - http://localhost:9002  
  - http://localhost:9003
- Tìm service `READING-WORKFLOW-SERVICE` trong danh sách "Instances currently registered with Eureka"
- Nếu không thấy, ReadingService chưa đăng ký thành công

### 3. Kiểm tra log của ReadingService
Tìm các log sau:
- "DiscoveryClient_READING-WORKFLOW-SERVICE" - đăng ký thành công
- "Registered Applications" - danh sách services đã đăng ký
- Lỗi connection đến Eureka

### 4. Nếu ReadingService chưa chạy:
```bash
cd services/ReadingService
./mvnw spring-boot:run
# hoặc
java -jar target/ReadingService-0.0.1-SNAPSHOT.jar
```

### 5. Nếu có lỗi database:
- Đảm bảo SQL Server đang chạy
- Kiểm tra connection string trong application.properties
- Có thể tạm thời comment `spring.jpa.hibernate.ddl-auto` để test

