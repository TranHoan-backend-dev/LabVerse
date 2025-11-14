# Tóm Tắt Luồng S3 - Câu Trả Lời Ngắn Gọn

## Câu Trả Lời Khi Thầy Hỏi: "S3 hoạt động như thế nào? Luồng là gì?"

---

## 1. Tổng Quan (30 giây)

**S3 được sử dụng để:**
- Lưu trữ PDF files của research papers
- Cung cấp public URL để truy cập PDF từ bất kỳ đâu
- Giảm tải cho database và server

---

## 2. Luồng Upload PDF lên S3 (2 phút)

### Bước 1: User Upload PDF
- User chọn file PDF từ device
- Frontend gửi file lên backend qua API: `POST /papers/pdf/upload-with-file`

### Bước 2: Backend Xử Lý
- **Validate**: Kiểm tra file là PDF, không rỗng
- **Upload lên S3**:
  - Tạo unique filename: `papers/{UUID}.pdf`
  - Upload file lên S3 bucket với ACL `PUBLIC_READ`
  - Nhận về S3 URL: `https://bucket.s3.region.amazonaws.com/papers/uuid.pdf`

### Bước 3: Lưu vào Database
- Lưu S3 URL vào field `dataUrl` của Paper entity
- Lưu metadata (title, authors, journal, etc.) vào database
- Trả về response thành công cho frontend

**Kết quả:** PDF được lưu trên S3, URL được lưu trong database

---

## 3. Luồng Hiển Thị PDF từ S3 (1 phút)

### Bước 1: User Click Xem Paper
- Frontend gọi API: `GET /papers/details?id={paper-id}`
- Backend trả về PaperResponse có chứa `dataUrl` (S3 URL)

### Bước 2: Load PDF từ S3
- **Android App**:
  - Download PDF từ S3 URL
  - Cache PDF locally để offline access
  - Hiển thị bằng PDF viewer library
  
- **Web App**:
  - Load PDF trực tiếp từ S3 URL (vì ACL là PUBLIC_READ)
  - Hiển thị bằng PDF.js hoặc iframe

**Kết quả:** User có thể xem PDF ngay cả khi offline (Android)

---

## 4. Điểm Quan Trọng (30 giây)

### Tại sao dùng S3?
- ✅ **Scalability**: Không giới hạn storage
- ✅ **Performance**: CDN support, load nhanh
- ✅ **Cost**: Chỉ trả tiền cho storage thực tế sử dụng
- ✅ **Reliability**: 99.99% uptime guarantee

### Security
- ✅ PDF files có ACL `PUBLIC_READ` → ai cũng có thể download
- ✅ Paper metadata vẫn được protect qua API authentication
- ✅ URLs có thể share được

### Offline Support
- ✅ Android app cache PDFs locally
- ✅ User có thể đọc PDF khi không có internet

---

## 5. Sơ Đồ Luồng (Visual)

```
┌─────────────┐
│   User      │
│  Upload PDF │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   Frontend      │
│  Send to API    │
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│   Backend       │─────▶│  AWS S3      │
│  Validate &     │      │  Upload File │
│  Process        │      │  Get URL     │
└──────┬──────────┘      └──────────────┘
       │                         │
       │                         │
       ▼                         │
┌─────────────────┐              │
│   Database      │◀──────────────┘
│  Save S3 URL    │
└─────────────────┘

Khi User xem PDF:
┌─────────────┐
│   User      │
│  Click Paper│
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│   Frontend      │
│  Get Paper Data│
└──────┬──────────┘
       │
       ▼
┌─────────────────┐      ┌──────────────┐
│   Backend       │      │  AWS S3      │
│  Return S3 URL  │─────▶│  Download PDF│
└─────────────────┘      └──────────────┘
       │                         │
       │                         │
       ▼                         │
┌─────────────────┐              │
│   Frontend      │◀──────────────┘
│  Display PDF   │
└─────────────────┘
```

---

## 6. Code Example (Nếu Thầy Hỏi Chi Tiết)

### Upload lên S3:
```kotlin
// S3Service.uploadPdf()
val fileName = "papers/${UUID.randomUUID()}.pdf"
val putObjectRequest = PutObjectRequest.builder()
    .bucket(bucketName)
    .key(fileName)
    .contentType("application/pdf")
    .acl(ObjectCannedACL.PUBLIC_READ)  // Public access
    .build()

s3Client.putObject(putObjectRequest, requestBody)
val s3Url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
```

### Lưu vào Database:
```kotlin
val paper = Paper(
    id = UUID.randomUUID(),
    dataUrl = s3Url,  // Lưu S3 URL
    metadata = Metadata(title, authors, ...),
    ...
)
paperRepo.save(paper)
```

---

## 7. Câu Trả Lời Ngắn Gọn (30 giây - Nếu thầy chỉ hỏi ngắn)

**"S3 hoạt động như sau:**

1. **Upload**: User upload PDF → Backend validate → Upload lên S3 → Nhận S3 URL → Lưu URL vào database

2. **Display**: User click xem paper → Backend trả về S3 URL → Frontend load PDF từ S3 URL → Hiển thị cho user

**Lợi ích**: 
- PDF không lưu trong database (giảm tải)
- Có thể cache offline
- Scalable và reliable"

---

## 8. Các Câu Hỏi Thường Gặp

### Q: Tại sao không lưu PDF trực tiếp trong database?
**A:** PDF files rất lớn, lưu trong database sẽ làm chậm và tốn chi phí. S3 chuyên dụng cho file storage.

### Q: Security như thế nào?
**A:** PDFs có ACL PUBLIC_READ để ai cũng có thể download, nhưng metadata (title, authors) vẫn được protect qua API authentication.

### Q: Offline hoạt động như thế nào?
**A:** Android app download và cache PDFs locally. Khi offline, app load từ cache thay vì S3.

### Q: Chi phí S3?
**A:** Chỉ trả tiền cho storage thực tế sử dụng, rất rẻ (khoảng $0.023/GB/tháng).

---

**Tài liệu này để trình bày ngắn gọn khi thầy hỏi về S3.**

