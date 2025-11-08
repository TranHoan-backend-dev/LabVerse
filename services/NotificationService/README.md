# Notification Service

Service xử lý thông báo cho hệ thống LabVerse, thay thế RabbitMQ bằng database-backed queue.

## 🚀 Cách gọi API từ các Service khác

### 1. Qua API Gateway (Khuyến nghị)

Nếu bạn có API Gateway chạy ở port 8080, gọi qua Gateway:

```java
// Spring Boot RestTemplate
@Service
public class YourService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void sendNotification(UUID userId, String title, String message, String linkTo) {
        String gatewayUrl = "http://localhost:8080"; // API Gateway URL
        
        NotificationRequestEvent event = NotificationRequestEvent.builder()
            .targetUserId(userId)
            .title(title)
            .message(message)
            .linkTo(linkTo)
            .build();
        
        restTemplate.postForEntity(
            gatewayUrl + "/v1/api/notifications/events",
            event,
            Void.class
        );
    }
}
```

**URL:** `POST http://localhost:8080/v1/api/notifications/events`

### 2. Qua Eureka Service Discovery (Internal)

Nếu gọi trực tiếp giữa các microservices (không qua Gateway):

```java
@Service
public class YourService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${notification.service.url}")
    private String notificationServiceUrl;
    
    public void sendNotification(UUID userId, String title, String message) {
        NotificationRequestEvent event = new NotificationRequestEvent();
        event.setTargetUserId(userId);
        event.setTitle(title);
        event.setMessage(message);
        
        // Nếu dùng Eureka, URL sẽ là service name
        restTemplate.postForEntity(
            "http://NOTIFICATION-SERVICE/v1/api/notifications/events",
            event,
            Void.class
        );
    }
}
```

### 3. Dùng Feign Client (Khuyến nghị cho Spring Cloud)

Tạo Feign Client:

```java
@FeignClient(name = "NOTIFICATION-SERVICE", path = "/v1/api/notifications")
public interface NotificationFeignClient {
    
    @PostMapping("/events")
    ResponseEntity<Void> createNotificationEvent(@RequestBody NotificationRequestEvent event);
}
```

Sử dụng:

```java
@Service
@RequiredArgsConstructor
public class YourService {
    
    private final NotificationFeignClient notificationClient;
    
    public void sendNotification(UUID userId, String title, String message) {
        NotificationRequestEvent event = NotificationRequestEvent.builder()
            .targetUserId(userId)
            .title(title)
            .message(message)
            .build();
        
        notificationClient.createNotificationEvent(event);
    }
}
```

## 📝 Request Body

```json
{
  "targetUserId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Thông báo mới",
  "message": "Bạn có thông báo mới từ hệ thống",
  "linkTo": "https://labverse.com/notification/123"
}
```

### Fields:
- `targetUserId` (UUID, required): ID của user nhận thông báo
- `title` (String, required): Tiêu đề thông báo
- `message` (String, required): Nội dung thông báo
- `linkTo` (String, optional): Link điều hướng khi click vào thông báo

## 📤 Response

- **Status Code:** `202 Accepted`
- **Body:** Empty

Lưu ý: Endpoint trả về `202 Accepted` vì notification được xử lý async qua queue.

## 🔧 Cấu hình cần thiết

### 1. Thêm DTO vào service của bạn

Tạo class `NotificationRequestEvent`:

```java
package com.yourpackage.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequestEvent {
    private UUID targetUserId;
    private String title;
    private String message;
    private String linkTo;
}
```

### 2. Cấu hình RestTemplate hoặc Feign

**RestTemplate:**
```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

**Feign Client:**
- Thêm dependency: `spring-cloud-starter-openfeign`
- Enable Feign: `@EnableFeignClients` trong main class

## 🐛 Troubleshooting

### Lỗi 503 Service Unavailable khi gọi qua Gateway

**Nguyên nhân:**
- Notification Service chưa đăng ký với Eureka
- Service name không khớp
- Service đang down

**Giải pháp:**

1. **Kiểm tra service đã đăng ký với Eureka:**
   - Truy cập: `http://localhost:9001` (hoặc 9002, 9003)
   - Tìm service `NOTIFICATION-SERVICE` trong danh sách

2. **Kiểm tra service name:**
   - Trong `application.properties`: `spring.application.name=NOTIFICATION-SERVICE`
   - Trong Gateway config: `lb://NOTIFICATION-SERVICE`

3. **Kiểm tra logs:**
   - Xem logs của Notification Service có lỗi không
   - Xem logs của Gateway Service

4. **Restart services theo thứ tự:**
   ```
   1. Eureka Server (9001, 9002, 9003)
   2. Notification Service
   3. API Gateway (8080)
   ```

5. **Kiểm tra network:**
   - Đảm bảo tất cả services trên cùng network
   - Firewall không block ports

### Lỗi Connection Refused

- Service chưa start
- Port bị conflict
- Database connection lỗi

## 📊 Kiểm tra Queue Status

Để xem queue status (debug/test):

```bash
GET http://localhost:8080/v1/api/notifications/queue
```

Hoặc với query param:
```bash
GET http://localhost:8080/v1/api/notifications/queue?status=PENDING
```

## 📚 Ví dụ sử dụng

### Ví dụ 1: Gửi notification khi có comment mới

```java
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final NotificationFeignClient notificationClient;
    
    public void createComment(UUID paperId, UUID userId, String content) {
        // ... logic tạo comment ...
        
        // Gửi notification cho tác giả paper
        NotificationRequestEvent event = NotificationRequestEvent.builder()
            .targetUserId(paperAuthorId)
            .title("Comment mới")
            .message(userName + " đã comment vào paper của bạn")
            .linkTo("/papers/" + paperId)
            .build();
        
        notificationClient.createNotificationEvent(event);
    }
}
```

### Ví dụ 2: Gửi notification khi có message mới

```java
@Service
public class MessageService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    public void sendMessage(UUID fromUserId, UUID toUserId, String message) {
        // ... logic gửi message ...
        
        NotificationRequestEvent event = new NotificationRequestEvent();
        event.setTargetUserId(toUserId);
        event.setTitle("Tin nhắn mới");
        event.setMessage("Bạn có tin nhắn mới từ " + fromUserName);
        event.setLinkTo("/messages");
        
        restTemplate.postForEntity(
            "http://localhost:8080/v1/api/notifications/events",
            event,
            Void.class
        );
    }
}
```

## 🔐 Security

Endpoint `/v1/api/notifications/events` đã được cấu hình `permitAll()` trong SecurityConfig, vì vậy:
- **Không cần JWT token** khi gọi từ các service khác
- Chỉ cần gọi đúng URL và format request body

## 📌 Lưu ý

1. **Async Processing:**
   - Notification được xử lý async qua queue
   - Không đợi kết quả ngay lập tức
   - Kiểm tra queue status nếu cần verify

2. **Retry Mechanism:**
   - Tự động retry tối đa 3 lần nếu lỗi
   - Check queue status với `status=FAILED` để xem lỗi

3. **Performance:**
   - Queue được xử lý mỗi 5 giây (có thể config)
   - Batch size: 10 notifications/lần (có thể config)

## 🛠️ Configuration

Trong `application.properties` của Notification Service:

```properties
# Queue processing config
app.queue.scheduler.enabled=true
app.queue.scheduler.interval=5000  # milliseconds
app.queue.batch-size=10
```

## 📞 Support

Nếu gặp vấn đề, kiểm tra:
1. Logs của Notification Service
2. Queue status: `/v1/api/notifications/queue`
3. Eureka dashboard: `http://localhost:9001`

