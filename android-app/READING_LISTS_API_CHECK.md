# Reading Lists API - Kiểm tra và Tổng hợp

## ✅ API Backend đã có sẵn

### 1. **POST /reading-service/reading-lists**
- **Mục đích**: Tạo reading list mới
- **Request Body**:
  ```json
  {
    "name": "List Name",
    "userIdsList": ["encoded_user_id_1", "encoded_user_id_2"], // Optional
    "paperIdsList": ["encoded_paper_id_1", "encoded_paper_id_2"] // Optional
  }
  ```
- **Response**: `ReadingListResponse` với encoded IDs
- **Status**: ✅ Đã tích hợp trong Android app

### 2. **GET /reading-service/reading-lists/user/{userId}**
- **Mục đích**: Lấy tất cả reading lists của một user
- **Path Parameter**: `userId` (encoded)
- **Response**: `List<ReadingListResponse>`
- **Status**: ✅ Đã tích hợp trong Android app

### 3. **PUT /reading-service/reading-lists/{listId}/papers**
- **Mục đích**: Thêm/xóa papers khỏi reading list
- **Path Parameter**: `listId` (encoded)
- **Request Body**:
  ```json
  {
    "action": "add" | "remove",
    "paperIds": ["encoded_paper_id_1", "encoded_paper_id_2"]
  }
  ```
- **Response**: `ReadingListResponse` updated
- **Status**: ✅ Đã tích hợp trong Android app (chưa sử dụng trong UI)

### 4. **PUT /reading-service/reading-lists/{listId}/users**
- **Mục đích**: Thêm/xóa users khỏi reading list
- **Path Parameter**: `listId` (encoded)
- **Request Body**:
  ```json
  {
    "action": "add" | "remove",
    "userIds": ["encoded_user_id_1", "encoded_user_id_2"]
  }
  ```
- **Response**: `ReadingListResponse` updated
- **Status**: ✅ Đã tích hợp trong Android app (chưa sử dụng trong UI)

### 5. **DELETE /reading-service/reading-lists/{listId}**
- **Mục đích**: Xóa reading list
- **Path Parameter**: `listId` (encoded)
- **Response**: Success message
- **Status**: ✅ Đã tích hợp trong Android app

---

## 📋 API Response Structure

### ReadingListResponse
```json
{
  "id": "encoded_list_id",
  "name": "List Name",
  "userIdsList": ["encoded_user_id_1", "encoded_user_id_2"],
  "paperIdsList": ["encoded_paper_id_1", "encoded_paper_id_2"],
  "createdAt": "2024-01-01T00:00:00",
  "updatedAt": "2024-01-01T00:00:00"
}
```

---

## 🔍 Tính năng UI đã implement

### ✅ Đã hoàn thành:
1. **Hiển thị danh sách Reading Lists**
   - RecyclerView với cards
   - Hiển thị tên list, số papers, số members
   - Empty state khi chưa có list nào

2. **Tạo Reading List mới**
   - Dialog từ dưới lên (bottom sheet style)
   - Input tên list
   - Nút "New List" và FAB

3. **Tìm kiếm**
   - Search bar với real-time filtering
   - Filter theo tên list

4. **Sắp xếp**
   - Sort theo: Name, Papers, Members, Date
   - Ascending/Descending

5. **Xóa Reading List**
   - Menu options (3 dots)
   - Confirmation dialog
   - Delete API call

6. **Share và View**
   - Nút Share (placeholder)
   - Nút View (placeholder - cần tạo ReadingListDetailActivity)

---

## ⚠️ API thiếu hoặc cần bổ sung

### 1. **GET /reading-service/reading-lists/{listId}** ❌
- **Mục đích**: Lấy chi tiết một reading list cụ thể
- **Cần thiết**: Để hiển thị chi tiết list khi click "View"
- **Status**: Chưa có trong backend

### 2. **PUT /reading-service/reading-lists/{listId}** ❌
- **Mục đích**: Cập nhật thông tin reading list (tên, mô tả)
- **Cần thiết**: Để edit tên list từ menu options
- **Status**: Chưa có trong backend

### 3. **GET /reading-service/reading-lists/{listId}/papers** ❌ (Optional)
- **Mục đích**: Lấy danh sách papers trong list (với metadata)
- **Cần thiết**: Để hiển thị chi tiết papers trong ReadingListDetailActivity
- **Status**: Có thể tính từ `paperIdsList` trong response, nhưng cần gọi Paper API để lấy metadata

### 4. **GET /reading-service/reading-lists/{listId}/users** ❌ (Optional)
- **Mục đích**: Lấy danh sách users trong list (với avatar, name)
- **Cần thiết**: Để hiển thị member avatars trong card
- **Status**: Có thể tính từ `userIdsList` trong response, nhưng cần gọi User API để lấy avatar URLs

### 5. **POST /reading-service/reading-lists/{listId}/share** ❌ (Optional)
- **Mục đích**: Tạo share link hoặc invite users
- **Cần thiết**: Để implement share functionality
- **Status**: Chưa có trong backend

---

## 📝 Ghi chú về Implementation

### Member Avatars
- Hiện tại: Chỉ hiển thị số lượng members
- Cần: Gọi User API để lấy avatar URLs từ `userIdsList`
- Workaround: Có thể implement sau khi có ReadingListDetailActivity

### Paper Details
- Hiện tại: Chỉ hiển thị số lượng papers
- Cần: Gọi Paper API để lấy metadata (title, authors) từ `paperIdsList`
- Workaround: Có thể implement sau khi có ReadingListDetailActivity

### Description Field
- Hiện tại: API không có field `description` trong `ReadingListResponse`
- Cần: Backend có thể thêm field này vào model và response
- Workaround: UI đã ẩn description field

---

## ✅ Kết luận

### API đủ để implement các tính năng cơ bản:
- ✅ Tạo reading list
- ✅ Hiển thị danh sách reading lists
- ✅ Tìm kiếm và sắp xếp
- ✅ Xóa reading list
- ✅ Thêm/xóa papers và users (API có sẵn, chưa dùng trong UI)

### API cần bổ sung để hoàn thiện:
- ❌ GET reading list detail (để View button)
- ❌ PUT update reading list (để Edit button)
- ⚠️ Share functionality (optional)

### Khuyến nghị:
1. **Ưu tiên**: Thêm API GET và PUT cho reading list detail/edit
2. **Sau đó**: Tạo ReadingListDetailActivity để hiển thị chi tiết
3. **Tùy chọn**: Implement share functionality nếu cần

---

## 🔗 Links liên quan

- Backend API Documentation: `services/ReadingService/API_TESTING_LINKS.md`
- Swagger UI: `http://localhost:8080/swagger-ui.html` (chọn reading-service)
- Android Implementation: `android-app/app/src/main/java/com/se1853_jv/labverse/presentation/readinglist/`

