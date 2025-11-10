# Hướng dẫn Test Notification Service với Postman

## Cấu hình cơ bản

### Base URL
- Local: `http://localhost:{PORT}`
- Hoặc dùng Eureka service name: `http://NOTIFICATION-SERVICE`

## Test Cases

### 1. Test Gửi Notification Event

**Request:**
- Method: `POST`
- URL: `{{baseUrl}}/v1/api/notifications/events`
- Headers:
  ```
  Content-Type: application/json
  ```
- Body (raw JSON):
```json
{
  "targetUserId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Thông báo mới",
  "message": "Bạn có thông báo mới từ hệ thống",
  "linkTo": "https://labverse.com/notification/123"
}
```

**Expected Response:**
- Status: `202 Accepted`
- Body: Empty

### 2. Test Xem Queue Status

**Request:**
- Method: `GET`
- URL: `{{baseUrl}}/v1/api/notifications/queue`
- Query Parameters (optional):
  - `status`: `PENDING`, `PROCESSING`, `COMPLETED`, hoặc `FAILED`

**Ví dụ:**
- Xem tất cả: `{{baseUrl}}/v1/api/notifications/queue`
- Xem pending: `{{baseUrl}}/v1/api/notifications/queue?status=PENDING`
- Xem completed: `{{baseUrl}}/v1/api/notifications/queue?status=COMPLETED`

**Expected Response:**
- Status: `200 OK`
- Body: Array of queue items

### 3. Test Flow Hoàn Chỉnh

1. Gửi notification event (Bước 1)
2. Ngay lập tức check queue → thấy status = `PENDING`
3. Đợi 5-10 giây
4. Check lại queue → thấy status = `COMPLETED` hoặc `PROCESSING`

## Sample Test Data

### UUID mẫu cho targetUserId
```
550e8400-e29b-41d4-a716-446655440000
6ba7b810-9dad-11d1-80b4-00c04fd430c8
6ba7b811-9dad-11d1-80b4-00c04fd430c8
```

### Test Cases khác nhau

**Test 1: Thông báo đơn giản**
```json
{
  "targetUserId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Test Notification",
  "message": "This is a test message",
  "linkTo": null
}
```

**Test 2: Thông báo có link**
```json
{
  "targetUserId": "550e8400-e29b-41d4-a716-446655440000",
  "title": "New Message",
  "message": "You have a new message",
  "linkTo": "https://labverse.com/messages/123"
}
```

## Troubleshooting

- Nếu không thấy queue items, kiểm tra:
  1. Service đã chạy chưa?
  2. Database connection OK chưa?
  3. Check logs của service

- Nếu status mãi ở PENDING:
  1. Check scheduled task có đang chạy không (logs)
  2. Có thể đang xử lý, đợi thêm vài giây
  3. Check có lỗi trong logs không

