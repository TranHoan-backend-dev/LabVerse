# Group Service HTTP Tests

## 🚀 Cách chạy test từ database trống

### Bước 1: Chạy Collection Tests trước
1. Mở file `collection-test.http`
2. Chạy các test theo thứ tự:
   - **Test 1-3**: Tạo collections (POST)
   - **Test 4-5**: Lấy danh sách collections (GET)
   - **Test 6**: Lấy collection by ID (GET) - **Copy ID từ response**
   - **Test 7-9**: Thêm và cập nhật papers (POST/PUT)

### Bước 2: Chạy Collection User Tests
1. Mở file `collection-user-test.http`
2. **Thay đổi collectionId** trong các test bằng ID thực tế từ bước 1
3. Chạy các test theo thứ tự:
   - **Test 1-3**: Thêm members vào collections (POST)
   - **Test 4-5**: Lấy danh sách members (GET)
   - **Test 6-7**: Xóa members (DELETE)

## 📝 Lưu ý quan trọng

- **Luôn chạy collection-test.http trước** để tạo dữ liệu cơ bản
- **Copy collectionId thực tế** từ response của test tạo collection
- **Thay đổi collectionId** trong collection-user-test.http bằng ID thực tế
- Các test validation error có thể chạy bất kỳ lúc nào

## 🔧 Cấu hình

- **URL Base**: `http://localhost:8080/v1/api`
- **Collection Endpoint**: `/group/collections`
- **Member Endpoint**: `/collections/members`
- **Port**: 8080 (có thể thay đổi trong application.properties)

## ✅ Kết quả mong đợi

- **200 OK**: Test thành công
- **400 Bad Request**: Lỗi validation (dự kiến)
- **404 Not Found**: Không tìm thấy resource (có thể xảy ra nếu chưa có dữ liệu)
- **500 Internal Server Error**: Lỗi server (cần kiểm tra logs)
