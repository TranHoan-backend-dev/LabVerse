# CORS Test Commands

Test CORS với các service khác nhau qua Gateway.

## 0. Test Paper Service (Main Issue)

```bash
# OPTIONS preflight - Đây là request quan trọng nhất
curl -X OPTIONS "http://localhost:8080/paper-service/papers/all?search=&index=1&size=12" \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: content-type" \
  -v

# GET request thực tế
curl -X GET "http://localhost:8080/paper-service/papers/all?search=&index=1&size=12" \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Content-Type: application/json" \
  -v
```

## 1. Test Account Service

```bash
# OPTIONS preflight
curl -X OPTIONS http://localhost:8080/account-service/api/users/me \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: content-type" \
  -v

# GET request
curl -X GET http://localhost:8080/account-service/api/users/me \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Content-Type: application/json" \
  -v
```

## 2. Test Reading Service

```bash
# OPTIONS preflight
curl -X OPTIONS http://localhost:8080/reading-service/reading-lists/user/test123 \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: content-type" \
  -v

# GET request
curl -X GET http://localhost:8080/reading-service/reading-lists/user/test123 \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Content-Type: application/json" \
  -v
```

## 3. Test Group Service

```bash
# OPTIONS preflight
curl -X OPTIONS http://localhost:8080/group-service/api/teams \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: content-type" \
  -v

# GET request
curl -X GET http://localhost:8080/group-service/api/teams \
  -H "Origin: http://192.168.1.18:3000" \
  -H "Content-Type: application/json" \
  -v
```

## Kiểm tra CORS Headers

Trong response, bạn cần thấy các header sau:
- `Access-Control-Allow-Origin: http://192.168.1.18:3000` (hoặc origin của bạn)
- `Access-Control-Allow-Methods: GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD`
- `Access-Control-Allow-Headers: *`
- `Access-Control-Allow-Credentials: true`

## Lưu ý

- Thay `http://192.168.1.18:3000` bằng origin thực tế của bạn
- Đảm bảo GatewayService đã được restart sau khi cập nhật CORS config
- Xóa cache trình duyệt nếu test từ browser

