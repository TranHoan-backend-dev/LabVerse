# Hướng dẫn test API Reading Service

## 📍 Các cách test API

### 1. Test qua Swagger UI (Trực tiếp ReadingService)

**Bước 1:** Tìm port của ReadingService
- Xem log khi start: `Tomcat started on port(s): XXXX`
- Hoặc xem Eureka Dashboard: http://localhost:9001

**Bước 2:** Truy cập Swagger UI
```
http://localhost:<PORT>/swagger-ui.html
```

**Bước 3:** Chọn server trong Swagger UI
- Swagger UI sẽ hiển thị dropdown "Servers"
- Chọn một trong 2 options:
  - **Gateway Server** (Recommended): `http://localhost:8080/v1/api/reading-service`
  - **Direct Service**: `/` (localhost với port động)

**Lưu ý:** Nếu chọn "Gateway Server", tất cả API calls sẽ đi qua Gateway.

---

### 2. Test qua Gateway (Không dùng Swagger UI)

**Base URL:**
```
http://localhost:8080/v1/api/reading-service
```

**Ví dụ API calls:**

#### Reading Workflow
```bash
# Create workflow
POST http://localhost:8080/v1/api/reading-service/workflows
Body: {
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id"
}

# Get workflows by user
GET http://localhost:8080/v1/api/reading-service/workflows/user/{encoded_userId}?status=reading

# Update progress
PUT http://localhost:8080/v1/api/reading-service/workflows/progress
Body: {
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id",
  "lastPage": 10,
  "progress": 50
}

# Update status
PATCH http://localhost:8080/v1/api/reading-service/workflows/status
Body: {
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id",
  "status": "reading"
}

# Delete workflow
DELETE http://localhost:8080/v1/api/reading-service/workflows
Body: {
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id"
}
```

#### Notes
```bash
# Add note
POST http://localhost:8080/v1/api/reading-service/notes
Body: {
  "content": "Note content",
  "coordinationX": 100,
  "coordinationY": 200,
  "pageNumber": 5,
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id"
}

# Get notes
GET http://localhost:8080/v1/api/reading-service/notes?collectionId={encoded}&paperId={encoded}&userId={encoded}

# Update note
PUT http://localhost:8080/v1/api/reading-service/notes/{encoded_note_id}
Body: {
  "content": "Updated content"
}

# Delete note
DELETE http://localhost:8080/v1/api/reading-service/notes/{encoded_note_id}
```

#### Highlights
```bash
# Add highlight
POST http://localhost:8080/v1/api/reading-service/highlights
Body: {
  "color": "#FFD700",
  "coordinationX": 150,
  "coordinationY": 250,
  "pageNumber": 7,
  "collectionId": "encoded_collection_id",
  "paperId": "encoded_paper_id",
  "userId": "encoded_user_id"
}

# Get highlights
GET http://localhost:8080/v1/api/reading-service/highlights?collectionId={encoded}&paperId={encoded}&userId={encoded}

# Delete highlight
DELETE http://localhost:8080/v1/api/reading-service/highlights/{encoded_highlight_id}
```

#### Reading Lists
```bash
# Create reading list
POST http://localhost:8080/v1/api/reading-service/reading-lists
Body: {
  "name": "My Reading List",
  "userIdsList": ["encoded_user_id_1", "encoded_user_id_2"],
  "paperIdsList": ["encoded_paper_id_1", "encoded_paper_id_2"]
}

# Get reading lists by user
GET http://localhost:8080/v1/api/reading-service/reading-lists/user/{encoded_userId}

# Update papers in list
PUT http://localhost:8080/v1/api/reading-service/reading-lists/{encoded_list_id}/papers
Body: {
  "action": "add",
  "paperIds": ["encoded_paper_id_3"]
}

# Update users in list
PUT http://localhost:8080/v1/api/reading-service/reading-lists/{encoded_list_id}/users
Body: {
  "action": "add",
  "userIds": ["encoded_user_id_3"]
}

# Delete reading list
DELETE http://localhost:8080/v1/api/reading-service/reading-lists/{encoded_list_id}
```

#### Summary
```bash
# Get reading summary
GET http://localhost:8080/v1/api/reading-service/summary/{encoded_userId}

# Get annotations
GET http://localhost:8080/v1/api/reading-service/summary/annotations/{encoded_paperId}/user/{encoded_userId}
```

---

### 3. Test từ Frontend (JavaScript/React/Angular)

**Base URL:**
```javascript
const API_BASE_URL = 'http://localhost:8080/v1/api/reading-service';
```

**Ví dụ với fetch:**
```javascript
// Create workflow
async function createWorkflow(collectionId, paperId, userId) {
  const response = await fetch(`${API_BASE_URL}/workflows`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      // 'Authorization': 'Bearer <token>' // Nếu có auth
    },
    body: JSON.stringify({
      collectionId: collectionId, // Đã được encode
      paperId: paperId,           // Đã được encode
      userId: userId              // Đã được encode
    })
  });
  return response.json();
}

// Get workflows by user
async function getWorkflows(userId, status) {
  const url = `${API_BASE_URL}/workflows/user/${userId}${status ? `?status=${status}` : ''}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    }
  });
  return response.json();
}

// Add note
async function addNote(noteData) {
  const response = await fetch(`${API_BASE_URL}/notes`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      content: noteData.content,
      coordinationX: noteData.coordinationX,
      coordinationY: noteData.coordinationY,
      pageNumber: noteData.pageNumber,
      collectionId: noteData.collectionId, // Encoded
      paperId: noteData.paperId,           // Encoded
      userId: noteData.userId              // Encoded
    })
  });
  return response.json();
}
```

**Ví dụ với Axios:**
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/v1/api/reading-service',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add interceptors nếu cần auth
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Usage
const createWorkflow = async (collectionId, paperId, userId) => {
  const response = await api.post('/workflows', {
    collectionId,
    paperId,
    userId
  });
  return response.data;
};
```

---

## 🔑 Lưu ý quan trọng

1. **Tất cả IDs phải được encode** trước khi gửi request (sử dụng `IdEncoder.encode()`)
2. **Base URL:** Luôn dùng `http://localhost:8080/v1/api/reading-service` khi gọi từ frontend
3. **Response format:** Tất cả responses đều wrap trong `WrapperApiResponse<T>`
   ```json
   {
     "success": true,
     "data": {...},
     "message": "..."
   }
   ```
4. **Error handling:** Kiểm tra `response.success` và `response.data` trong frontend

---

## 🧪 Test với Postman/Thunder Client

1. Import OpenAPI spec từ: `http://localhost:8080/v1/api/reading-service/v3/api-docs`
2. Set base URL: `http://localhost:8080/v1/api/reading-service`
3. Tất cả requests sẽ tự động có base URL đúng

---

## 📝 Example: Complete flow

```javascript
// 1. Create workflow
const workflow = await createWorkflow(
  IdEncoder.encode('collection-uuid'),
  IdEncoder.encode('paper-uuid'),
  IdEncoder.encode('user-uuid')
);

// 2. Add note
const note = await addNote({
  content: "Important note",
  coordinationX: 100,
  coordinationY: 200,
  pageNumber: 5,
  collectionId: IdEncoder.encode('collection-uuid'),
  paperId: IdEncoder.encode('paper-uuid'),
  userId: IdEncoder.encode('user-uuid')
});

// 3. Get all notes for a paper
const notes = await fetch(
  `${API_BASE_URL}/notes?collectionId=${encoded_collection_id}&paperId=${encoded_paper_id}&userId=${encoded_user_id}`
).then(r => r.json());
```

