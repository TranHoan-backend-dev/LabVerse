# 📍 Link Test API Reading Service

## 📚 Swagger UI - API Documentation

### ✅ Qua Gateway Swagger UI (Recommended - Dùng Gateway's Swagger):
**Gateway Swagger UI (tất cả services):** `http://localhost:8080/swagger-ui.html`

- Gateway đã tích hợp Swagger UI với tất cả services
- Chọn **"reading-service"** trong dropdown để xem Reading Service APIs
- OpenAPI JSON: `http://localhost:8080/reading-service/v3/api-docs`

### 🔧 Trực tiếp (Direct - chỉ khi Gateway không hoạt động):
Tìm dynamic port trong log, ví dụ: `Tomcat started on port(s): 54321`

**Swagger UI:** `http://localhost:54321/v1/api/swagger-ui.html` (có context-path `/v1/api`)

**OpenAPI JSON:** `http://localhost:54321/v1/api/v3/api-docs`

**Lưu ý:** 
- Gateway's Swagger UI là cách tốt nhất để test API
- Trong Gateway Swagger UI, chọn server **"reading-service"** từ dropdown

---

## 🎯 Qua Gateway (Recommended)

Base URL: `http://localhost:8080/reading-service`

### Endpoints:

1. **Reading Workflow:**
   - `GET http://localhost:8080/reading-service/workflows` - Get all workflows
   - `POST http://localhost:8080/reading-service/workflows` - Create workflow
   - `GET http://localhost:8080/reading-service/workflows/user/{userId}` - Get workflows by user
   - `PUT http://localhost:8080/reading-service/workflows/progress` - Update progress
   - `PATCH http://localhost:8080/reading-service/workflows/status` - Update status
   - `DELETE http://localhost:8080/reading-service/workflows` - Delete workflow

2. **Notes:**
   - `GET http://localhost:8080/reading-service/notes?collectionId=...&paperId=...&userId=...`
   - `POST http://localhost:8080/reading-service/notes`
   - `PUT http://localhost:8080/reading-service/notes/{noteId}`
   - `DELETE http://localhost:8080/reading-service/notes/{noteId}`

3. **Highlights:**
   - `GET http://localhost:8080/reading-service/highlights?collectionId=...&paperId=...&userId=...`
   - `POST http://localhost:8080/reading-service/highlights`
   - `DELETE http://localhost:8080/reading-service/highlights/{highlightId}`

4. **Reading Lists:**
   - `GET http://localhost:8080/reading-service/reading-lists/user/{userId}`
   - `POST http://localhost:8080/reading-service/reading-lists`
   - `PUT http://localhost:8080/reading-service/reading-lists/{listId}/papers`
   - `PUT http://localhost:8080/reading-service/reading-lists/{listId}/users`
   - `DELETE http://localhost:8080/reading-service/reading-lists/{listId}`

5. **Summary:**
   - `GET http://localhost:8080/reading-service/summary/{userId}`
   - `GET http://localhost:8080/reading-service/summary/annotations/{paperId}/user/{userId}`

## 🔧 Trực tiếp (Direct - chỉ khi Gateway không hoạt động)

Tìm dynamic port trong log Reading Service, ví dụ: `Tomcat started on port(s): 54321`

Base URL: `http://localhost:54321` (thay 54321 bằng port thực tế)

Endpoints với context-path `/v1/api`, ví dụ:
- `GET http://localhost:54321/v1/api/workflows`
- `POST http://localhost:54321/v1/api/workflows`
- `GET http://localhost:54321/v1/api/swagger-ui.html` (Swagger UI)

## 📝 Lưu ý:

1. **IDs phải được encode** (Base64 URL-safe) khi gửi qua API
2. **User ID field**: `usersid` (không phải `userId`)
3. **Status values**: `"unread"`, `"reading"`, `"finished"` (không phải `"IN_PROGRESS"`)

## ✅ Example Request:

```bash
curl -X POST 'http://localhost:8080/reading-service/workflows' \
  -H 'Content-Type: application/json' \
  -d '{
    "collectionId": "550e8400-e29b-41d4-a716-446655440000",
    "paperId": "660e8400-e29b-41d4-a716-446655440001",
    "usersid": "770e8400-e29b-41d4-a716-446655440002",
    "status": "reading",
    "lastPage": 1,
    "progress": 10
  }'
```

