# Mô Tả Luồng Hoạt Động - Team và Reading List

## Mục Lục
1. [Tổng Quan](#tổng-quan)
2. [Luồng Hoạt Động Team Management](#luồng-hoạt-động-team-management)
3. [Luồng Hoạt Động Reading List Management](#luồng-hoạt-động-reading-list-management)
4. [So Sánh với Core Functional.md](#so-sánh-với-core-functionalmd)
5. [Luồng Upload PDF lên S3 và Hiển Thị](#luồng-upload-pdf-lên-s3-và-hiển-thị)

---

## Tổng Quan

Tài liệu này mô tả chi tiết các luồng hoạt động của **Team Management** và **Reading List Management** trong hệ thống LabVerse, đồng thời so sánh với yêu cầu trong Core Functional.md và mô tả luồng upload PDF lên S3 và hiển thị.

---

## Luồng Hoạt Động Team Management

### 1. Tạo Team Mới (UC-TM-01)

**Luồng chính:**
```
1. User đăng nhập → Navigate đến trang Teams (/teams)
2. Click nút "Create Team"
3. Hệ thống hiển thị dialog tạo team
4. User điền form:
   - Team Name (bắt buộc)
   - Description (tùy chọn)
   - Research Field (tùy chọn)
   - Privacy: PUBLIC hoặc PRIVATE (bắt buộc)
5. Click "Create Team"
6. Frontend gửi POST request đến API: POST /v1/api/teams
7. Backend (AccountService) xử lý:
   - Validate dữ liệu đầu vào
   - Kiểm tra tên team đã tồn tại chưa
   - Tạo team trong database với UUID
   - Tự động thêm creator làm OWNER
   - Encode ID trước khi trả về
8. Backend trả về TeamResponse với ID đã encode
9. Frontend hiển thị thông báo thành công
10. Refresh danh sách teams
11. Đóng dialog
```

**Business Rules:**
- Tên team phải unique trong hệ thống
- Creator tự động nhận role OWNER
- ID được encode bằng Base64 URL-safe trước khi trả về
- Privacy mặc định là PUBLIC nếu không chỉ định

**API Endpoint:**
- `POST /account-service/v1/api/teams`
- Authentication: Required (Bearer Token)
- Authorization: `@PreAuthorize("isAuthenticated()")`

---

### 2. Xem Danh Sách Teams (UC-TM-02)

**Luồng chính:**
```
1. User navigate đến trang Teams
2. Frontend gửi GET request: GET /v1/api/teams/?page=0&size=10-12
3. Có thể thêm filters:
   - search: Tìm kiếm theo tên team
   - privacy: Lọc theo PUBLIC/PRIVATE
   - researchField: Lọc theo lĩnh vực nghiên cứu
4. Backend trả về paginated response với danh sách teams
5. Frontend hiển thị team cards trong grid layout
6. User có thể:
   - Scroll để xem thêm
   - Search theo tên
   - Filter theo privacy/research field
   - Click pagination để chuyển trang
   - Click vào team card để xem chi tiết
```

**Business Rules:**
- PUBLIC teams: Tất cả user đã đăng nhập đều thấy được
- PRIVATE teams: Chỉ members mới thấy được
- Pagination mặc định: 10-12 teams/trang
- Search không phân biệt hoa thường

---

### 3. Xem Chi Tiết Team (UC-TM-03)

**Luồng chính:**
```
1. User click vào team card
2. Navigate đến /teams/:id
3. Frontend gửi GET request: GET /v1/api/teams/{teamId}
4. Backend decode teamId và lấy thông tin team
5. Frontend gửi GET request: GET /v1/api/teams/{teamId}/members
6. Backend trả về danh sách members với roles
7. Frontend hiển thị:
   - Thông tin team (name, description, research field, privacy)
   - Danh sách members với roles
   - Paper count (nếu có)
   - Action buttons (nếu user là OWNER/ADMIN):
     * Add Member
     * Edit Team
     * Delete Team
     * Update Member Role
```

**Business Rules:**
- Chỉ OWNER và ADMIN thấy action buttons
- MEMBER chỉ có thể xem thông tin
- PRIVATE teams chỉ members mới truy cập được

---

### 4. Thêm Member vào Team (UC-TM-04)

**Luồng chính:**
```
1. OWNER/ADMIN navigate đến team detail page
2. Click "Add Member"
3. Hiển thị dialog thêm member
4. User nhập email hoặc username
5. User chọn role: ADMIN hoặc MEMBER
6. Click "Add"
7. Frontend gửi POST request: POST /v1/api/teams/{teamId}/members
   Body: { "userId": "email-or-id", "role": "MEMBER" }
8. Backend xử lý:
   - Tìm user theo email/username
   - Kiểm tra user đã là member chưa
   - Thêm user vào team với role đã chọn
9. Backend trả về TeamMemberResponse
10. Frontend refresh member list
11. Hiển thị thông báo thành công
```

**Business Rules:**
- OWNER không thể remove hoặc đổi role của chính mình
- Mỗi user chỉ có thể là member một lần mỗi team
- Role mặc định là MEMBER nếu không chỉ định
- OWNER role không thể assign qua action này (chỉ qua transfer)

---

### 5. Xóa Member khỏi Team (UC-TM-05)

**Luồng chính:**
```
1. OWNER/ADMIN xem member list
2. Click "Remove" bên cạnh member
3. Hiển thị confirmation dialog
4. User xác nhận
5. Frontend gửi DELETE request: DELETE /v1/api/teams/{teamId}/members/{memberId}
6. Backend xử lý:
   - Kiểm tra quyền (OWNER/ADMIN)
   - Kiểm tra không phải OWNER
   - Xóa member khỏi team
   - Revoke access của member
7. Backend trả về success
8. Frontend refresh member list
9. Hiển thị thông báo thành công
```

**Business Rules:**
- OWNER không thể remove chính mình
- OWNER không thể bị remove bởi ADMIN
- Xóa là immediate và không thể undo

---

### 6. Cập Nhật Role của Member (UC-TM-06)

**Luồng chính:**
```
1. OWNER xem member list
2. Click "Change Role" hoặc dropdown role
3. Chọn role mới: OWNER, ADMIN, hoặc MEMBER
4. Hiển thị confirmation (đặc biệt khi transfer OWNER)
5. User xác nhận
6. Frontend gửi PUT request: PUT /v1/api/teams/{teamId}/members/{memberId}/role
   Body: { "role": "ADMIN" }
7. Backend xử lý:
   - Kiểm tra quyền (chỉ OWNER)
   - Nếu transfer OWNER:
     * Cập nhật role của member mới thành OWNER
     * Cập nhật role của OWNER cũ thành ADMIN
   - Cập nhật permissions
8. Backend trả về updated TeamMemberResponse
9. Frontend refresh member list
```

**Business Rules:**
- Chỉ OWNER mới có thể đổi role
- Khi transfer OWNER, OWNER cũ tự động thành ADMIN
- Chỉ có một OWNER tại một thời điểm
- Role changes là immediate

---

### 7. Cập Nhật Thông Tin Team (UC-TM-07)

**Luồng chính:**
```
1. OWNER/ADMIN navigate đến team detail
2. Click "Edit Team"
3. Hiển thị form với thông tin hiện tại
4. User chỉnh sửa:
   - Team Name
   - Description
   - Research Field
   - Privacy (PUBLIC/PRIVATE)
5. Click "Save"
6. Frontend gửi PUT request: PUT /v1/api/teams/{teamId}
   Body: { "name": "...", "description": "...", ... }
7. Backend xử lý:
   - Validate dữ liệu
   - Kiểm tra tên team unique (nếu đổi tên)
   - Cập nhật team trong database
8. Backend trả về updated TeamResponse
9. Frontend refresh thông tin team
10. Hiển thị thông báo thành công
```

**Business Rules:**
- Tên team phải unique nếu đổi
- Privacy có thể đổi giữa PUBLIC và PRIVATE
- Updated timestamp tự động cập nhật

---

### 8. Xóa Team (UC-TM-08)

**Luồng chính:**
```
1. OWNER navigate đến team detail
2. Click "Delete Team"
3. Hiển thị confirmation dialog với warning
4. User xác nhận
5. Frontend gửi DELETE request: DELETE /v1/api/teams/{teamId}
6. Backend xử lý:
   - Kiểm tra quyền (chỉ OWNER)
   - Xóa team khỏi database
   - Xóa tất cả team-member relationships
   - Xử lý team-paper relationships (papers không bị xóa)
7. Backend trả về success
8. Frontend navigate về team list
9. Hiển thị thông báo thành công
```

**Business Rules:**
- Chỉ OWNER mới có thể xóa team
- Xóa là permanent và không thể undo
- Papers không bị xóa, chỉ mất association với team
- Tất cả members mất access ngay lập tức

---

## Luồng Hoạt Động Reading List Management

### 1. Tạo Reading List (UC-RL-01)

**Luồng chính:**
```
1. User đăng nhập → Navigate đến Reading Lists page (/reading-lists)
2. Click "Create List"
3. Hiển thị dialog tạo reading list
4. User điền form:
   - List Name (bắt buộc)
   - Description (tùy chọn)
5. Click "Create List"
6. Frontend gửi POST request: POST /v1/api/reading-lists
   Body: {
     "name": "...",
     "description": "...",
     "userId": "encoded-user-id",
     "userIdsList": ["encoded-user-id"],
     "paperIdsList": []
   }
7. Backend (ReadingService) xử lý:
   - Validate dữ liệu (name required)
   - Tạo reading list trong database
   - Tự động thêm creator vào userIds array
   - Encode ID trước khi trả về
8. Backend trả về ReadingListResponse
9. Frontend refresh danh sách reading lists
10. Hiển thị thông báo thành công
```

**Business Rules:**
- List name không cần unique (user có thể có nhiều list cùng tên)
- Creator tự động được thêm vào userIds array
- List bắt đầu với paperIds array rỗng
- Created và updated timestamps tự động set

---

### 2. Xem Danh Sách Reading Lists (UC-RL-02)

**Luồng chính:**
```
1. User navigate đến Reading Lists page
2. Frontend gửi GET request: GET /v1/api/reading-lists/user/{userId}
3. Backend trả về danh sách reading lists mà user là member
4. Frontend hiển thị reading list cards trong grid layout
5. Mỗi card hiển thị:
   - List name
   - Description (truncated 2 dòng)
   - Paper count
   - Member count
   - Created date
6. User có thể click vào card để xem chi tiết
```

**Business Rules:**
- Chỉ hiển thị lists mà user là member
- Lists được sort theo created date (mới nhất trước)
- Empty lists vẫn được hiển thị
- Paper count và member count hiển thị 0 nếu rỗng

---

### 3. Xem Chi Tiết Reading List (UC-RL-03)

**Luồng chính:**
```
1. User click vào reading list card
2. Navigate đến /reading-lists/:id
3. Frontend gửi GET request: GET /v1/api/reading-lists/{listId}
4. Backend decode listId và lấy thông tin list
5. Frontend hiển thị:
   - List name và description
   - Paper count
   - Member count
   - Danh sách papers (title, authors, journal)
   - Danh sách members (name, email, avatar)
   - Created date, Updated date
6. Hiển thị action buttons:
   - Add Paper
   - Add Member
   - Delete List
```

**Business Rules:**
- Chỉ members mới có thể xem chi tiết
- Tất cả members có quyền ngang nhau (không có role hierarchy)
- Papers hiển thị với thông tin cơ bản

---

### 4. Thêm Papers vào Reading List (UC-RL-04)

**Luồng chính:**
```
1. Member navigate đến reading list detail
2. Click "Add Paper"
3. Hiển thị paper selection interface:
   - Search bar để tìm papers
   - Danh sách papers từ library của user
   - Hoặc paper picker dialog
4. User chọn một hoặc nhiều papers
5. Click "Add" hoặc "Add Selected"
6. Frontend gửi PUT request: PUT /v1/api/reading-lists/{listId}/papers
   Body: { "paperIds": ["encoded-paper-id-1", "encoded-paper-id-2"] }
7. Backend xử lý:
   - Validate papers tồn tại
   - Kiểm tra papers đã có trong list chưa (optional)
   - Thêm papers vào paperIds array
   - Cập nhật reading list trong database
8. Backend trả về updated ReadingListResponse
9. Frontend refresh paper list
10. Hiển thị thông báo thành công
```

**Business Rules:**
- Một paper có thể tồn tại trong nhiều reading lists
- Papers không bị duplicate trong cùng một list
- Thêm papers không xóa papers khỏi user's library
- Paper count tự động cập nhật

---

### 5. Xóa Papers khỏi Reading List (UC-RL-05)

**Luồng chính:**
```
1. Member xem paper list trong reading list detail
2. Click "Remove" trên paper card hoặc swipe to delete (mobile)
3. Hiển thị confirmation (optional)
4. User xác nhận
5. Frontend gửi PUT request: PUT /v1/api/reading-lists/{listId}/papers
   Body: { "paperIds": [remaining-paper-ids] } // Không bao gồm paper bị xóa
6. Backend cập nhật paperIds array
7. Frontend refresh paper list
8. Hiển thị thông báo thành công
```

**Business Rules:**
- Xóa paper khỏi list không xóa paper khỏi system
- Paper vẫn ở trong user's library
- Paper vẫn ở trong các reading lists khác (nếu có)
- Xóa là immediate

---

### 6. Thêm Members vào Reading List (UC-RL-06)

**Luồng chính:**
```
1. Member navigate đến reading list detail
2. Click "Add Member"
3. Hiển thị dialog thêm member
4. User nhập email hoặc username
5. Click "Add"
6. Frontend gửi PUT request: PUT /v1/api/reading-lists/{listId}/users
   Body: { "userIds": ["encoded-user-id-1", "encoded-user-id-2"] }
7. Backend xử lý:
   - Tìm user theo email/username
   - Validate user tồn tại
   - Kiểm tra user đã là member chưa
   - Thêm user vào userIds array
   - Cập nhật reading list trong database
8. Backend trả về updated ReadingListResponse
9. Frontend refresh member list
10. Hiển thị thông báo thành công
```

**Business Rules:**
- Mỗi user chỉ có thể là member một lần mỗi list
- Tất cả members có quyền ngang nhau
- Thêm members cho phép collaborative list management
- Members có thể truy cập list ngay lập tức

---

### 7. Xóa Members khỏi Reading List (UC-RL-07)

**Luồng chính:**
```
1. Member xem member list trong reading list detail
2. Click "Remove" bên cạnh user
3. Hiển thị confirmation dialog
4. User xác nhận
5. Frontend gửi PUT request: PUT /v1/api/reading-lists/{listId}/users
   Body: { "userIds": [remaining-user-ids] } // Không bao gồm user bị xóa
6. Backend cập nhật userIds array
7. Frontend refresh member list
8. Hiển thị thông báo thành công
```

**Business Rules:**
- Users có thể remove chính mình khỏi list
- Xóa member không xóa papers
- Papers vẫn accessible cho remaining members
- Xóa là immediate

---

### 8. Xóa Reading List (UC-RL-08)

**Luồng chính:**
```
1. Member navigate đến Reading Lists page hoặc detail page
2. Click "Delete" (dropdown menu hoặc action button)
3. Hiển thị confirmation dialog với warning
4. User xác nhận
5. Frontend gửi DELETE request: DELETE /v1/api/reading-lists/{listId}
6. Backend xử lý:
   - Xóa reading list khỏi database
   - Xóa tất cả list-user relationships
   - Xử lý list-paper relationships (papers không bị xóa)
7. Backend trả về success
8. Frontend navigate về reading lists page
9. Hiển thị thông báo thành công
```

**Business Rules:**
- Bất kỳ member nào cũng có thể xóa list (không cần OWNER)
- Xóa là permanent và không thể undo
- Papers trong list không bị xóa khỏi system
- Papers vẫn ở trong user libraries và các lists khác
- Tất cả list data bị xóa

---

## So Sánh với Core Functional.md

### 1. Team Management

**Core Functional.md - Activity 6: Create and Manage Shared Collections (Projects)**

**Yêu cầu trong Core Functional.md:**
- Purpose: Cho phép teams nhóm papers liên quan đến một project hoặc topic cụ thể
- UI Components:
  - Tab "Teams" hoặc "Collections"
  - RecyclerView liệt kê tất cả shared collections mà user là thành viên
  - Button cho PIs để "Create New Collection" và "Invite Members" qua email
- Core Logic: Collections là backend feature. App fetch danh sách collections mà user có access. Thêm paper vào collection link nó với collection's ID.

**Ánh xạ với Use Case Specification:**

✅ **Đã ánh xạ đầy đủ:**

1. **Create Team (UC-TM-01)** ↔ **Create New Collection**
   - ✅ User có thể tạo team mới với thông tin cơ bản
   - ✅ Creator tự động trở thành OWNER (tương đương PI)
   - ✅ Team có thể PUBLIC hoặc PRIVATE

2. **View Team List (UC-TM-02)** ↔ **RecyclerView của Collections**
   - ✅ Hiển thị danh sách teams mà user là member
   - ✅ Hỗ trợ search và filter
   - ✅ Pagination support

3. **Add Team Member (UC-TM-04)** ↔ **Invite Members**
   - ✅ OWNER/ADMIN có thể thêm members
   - ✅ Có thể thêm bằng email/username
   - ⚠️ Chưa có email invitation workflow (được đề cập trong Future Enhancements)

4. **Team-Paper Relationship**
   - ✅ Papers có thể được link với team (thông qua collections)
   - ✅ Papers không bị xóa khi team bị xóa

**Khác biệt và Bổ sung:**

- ✅ **Role Hierarchy**: Use Case Specification chi tiết hơn với OWNER > ADMIN > MEMBER
- ✅ **Privacy Settings**: PUBLIC/PRIVATE teams
- ✅ **Research Field**: Thêm field để categorize teams
- ✅ **Member Role Management**: Chi tiết hơn với transfer ownership
- ⚠️ **Email Invitations**: Chưa implement (trong Future Enhancements)

---

### 2. Reading List Management

**Core Functional.md - Activity 15: Create Reading Lists & Journal Clubs**

**Yêu cầu trong Core Functional.md:**
- Purpose: Cho phép users manually curate và share themed collections của papers cho specific projects hoặc discussion groups
- UI Components:
  - Tab "Reading Lists" với RecyclerView của user-created lists
  - FAB để "Create New List"
  - Interface đơn giản để add papers từ main library vào list
  - Share button để invite other users để view hoặc collaborate trên list
- Core Logic: Feature dựa trên database relationships đơn giản (list has many papers; user has many lists). Tất cả actions là CRUD operations qua API.

**Ánh xạ với Use Case Specification:**

✅ **Đã ánh xạ đầy đủ:**

1. **Create Reading List (UC-RL-01)** ↔ **Create New List**
   - ✅ User có thể tạo reading list mới
   - ✅ Creator tự động được thêm vào members
   - ✅ List bắt đầu với empty papers

2. **View Reading Lists (UC-RL-02)** ↔ **RecyclerView của Lists**
   - ✅ Hiển thị danh sách reading lists của user
   - ✅ Hiển thị paper count và member count

3. **Add Papers to Reading List (UC-RL-04)** ↔ **Add Papers từ Library**
   - ✅ Members có thể thêm papers vào list
   - ✅ Papers được lấy từ user's library
   - ✅ Hỗ trợ multiple papers selection

4. **Add Members to Reading List (UC-RL-06)** ↔ **Share Button**
   - ✅ Members có thể thêm users khác vào list
   - ✅ Hỗ trợ collaborative management
   - ⚠️ Chưa có share link/public link (trong Future Enhancements)

5. **Database Relationships**
   - ✅ List has many papers (paperIds array)
   - ✅ User has many lists (userIds array)
   - ✅ Tất cả operations là CRUD qua API

**Khác biệt và Bổ sung:**

- ✅ **Equal Permissions**: Tất cả members có quyền ngang nhau (không có role hierarchy như teams)
- ✅ **Multiple Lists per User**: User có thể có nhiều lists với cùng tên
- ✅ **Paper Sharing**: Papers có thể ở nhiều lists cùng lúc
- ⚠️ **Journal Clubs**: Chưa có feature riêng cho journal clubs (có thể implement như reading list với discussion features)

---

## Luồng Upload PDF lên S3 và Hiển Thị

### 1. Luồng Upload PDF lên S3

**Tổng quan:**
Hệ thống sử dụng AWS S3 để lưu trữ PDF files. Khi user upload PDF, file được upload lên S3 bucket và URL được lưu trong database.

**Luồng chi tiết:**

```
1. User chọn PDF file từ device
   - Android: Sử dụng Intent để mở file picker
   - Web: Sử dụng file input element

2. Frontend gửi request upload
   - Endpoint: POST /paper-service/v1/api/papers/pdf/upload-with-file
   - Method: POST
   - Content-Type: multipart/form-data
   - Headers:
     * X-User-Id: encoded-user-id (optional)
   - Body (form-data):
     * file: PDF file (MultipartFile)
     * title: String (required)
     * authors: String (required)
     * journal: String (required)
     * publicationYear: Int (required)
     * doi: String (required)
     * description: String (optional)
     * keywords: String (optional, comma-separated)
     * tags: String (optional, comma-separated)

3. Backend Controller (PaperController.uploadPdfWithFile)
   - Validate file:
     * Kiểm tra file không rỗng
     * Kiểm tra content-type là "application/pdf"
   - Kiểm tra S3Service có available không
   - Gọi S3Service.uploadPdf()

4. S3Service.uploadPdf()
   - Tạo unique filename: "papers/{UUID}.pdf"
   - Đọc file từ InputStream vào ByteArray
   - Build PutObjectRequest:
     * bucket: bucketName từ config
     * key: fileName
     * contentType: "application/pdf"
     * acl: PUBLIC_READ (public-read)
   - Upload file lên S3:
     * Sử dụng S3Client.putObject()
     * RequestBody từ ByteArray
   - Generate S3 URL:
     * Nếu region = "us-east-1":
       "https://{bucketName}.s3.amazonaws.com/{fileName}"
     * Nếu region khác:
       "https://{bucketName}.s3.{region}.amazonaws.com/{fileName}"
   - Return S3 URL

5. Backend Controller tiếp tục xử lý
   - Parse keywords và tags (nếu có)
   - Tạo UploadPdfRequest với:
     * dataUrl: S3 URL từ bước 4
     * title, authors, journal, publicationYear, doi
     * description, keywords, tags
   - Gọi PaperService.createNewPaper()

6. PaperService.createNewPaper()
   - Generate DOI nếu không có hoặc empty
   - Process tags: Tìm hoặc tạo tags trong database
   - Build Paper entity:
     * id: UUID.randomUUID()
     * dataUrl: S3 URL
     * metadata: Metadata(title, authors, journal, publicationYear, doi)
     * keywords, description, tags
     * createdBy: userId
   - Save Paper vào database (SQL Server)
   - Sync với Firebase (nếu configured)

7. Backend trả về response
   - Status: 200 OK
   - Message: "Upload paper successfully"
   - Data: null

8. Frontend xử lý response
   - Hiển thị thông báo thành công
   - Refresh paper list
   - Navigate về library page (optional)
```

**Cấu hình S3:**

```kotlin
// S3Config.kt
@Configuration
@ConditionalOnProperty(prefix = "aws.s3", name = ["access-key"])
class S3Config(
    @Value("\${aws.s3.access-key}") private val accessKey: String,
    @Value("\${aws.s3.secret-key}") private val secretKey: String,
    @Value("\${aws.s3.region}") private val region: String,
    @Value("\${aws.s3.bucket}") private val bucketName: String
) {
    @Bean
    fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .build()
    }
}
```

**S3 Bucket Settings:**
- **ACL**: PUBLIC_READ được set khi upload
- **Object Ownership**: Phải là "Bucket owner preferred" hoặc "ACLs enabled"
- **Block Public ACLs**: Phải disabled để cho phép public-read
- **Region**: Có thể config bất kỳ AWS region nào

**Error Handling:**
- Nếu S3Service không available: Trả về 503 Service Unavailable
- Nếu file không phải PDF: Trả về 400 Bad Request
- Nếu upload S3 fail: Log error và throw RuntimeException
- Nếu ACL fail: Log warning về bucket settings

---

### 2. Luồng Hiển Thị PDF

**Tổng quan:**
Sau khi PDF được upload lên S3, URL được lưu trong database. Khi user muốn xem PDF, app fetch URL từ API và hiển thị PDF từ S3 URL.

**Luồng chi tiết:**

```
1. User click vào paper trong library/list
   - Android: Navigate đến PaperDetailsActivity
   - Web: Navigate đến paper detail page

2. Frontend fetch paper details
   - Endpoint: GET /paper-service/v1/api/papers/details?id={encoded-paper-id}
   - Backend trả về PaperResponse:
     {
       "id": "encoded-id",
       "dataUrl": "https://bucket.s3.region.amazonaws.com/papers/uuid.pdf",
       "title": "...",
       "authors": "...",
       "journal": "...",
       "publicationYear": 2024,
       "doi": "...",
       "keywords": [...],
       "description": "..."
     }

3. Frontend lấy dataUrl từ response
   - dataUrl là S3 URL của PDF file
   - URL có format: https://{bucket}.s3.{region}.amazonaws.com/papers/{uuid}.pdf

4. Android App hiển thị PDF
   - Sử dụng PDF viewer library (AndroidPdfViewer hoặc tương tự)
   - Load PDF từ URL:
     * Download PDF từ S3 URL
     * Cache PDF locally (Room database hoặc file system)
     * Render PDF trong PDFView component
   - Features:
     * Zoom in/out
     * Scroll pages
     * Annotations (highlights, notes)
     * Page navigation

5. Web App hiển thị PDF
   - Sử dụng PDF.js hoặc iframe
   - Load PDF từ S3 URL:
     * Fetch PDF từ S3 URL
     * Render trong <iframe> hoặc <embed>
     * Hoặc sử dụng PDF.js để render
   - Features:
     * Zoom in/out
     * Page navigation
     * Download option
     * Annotations (nếu implement)

6. Offline Access (Android)
   - PDF được cache locally sau khi download
   - Khi offline, app load PDF từ cache
   - Annotations được lưu locally và sync khi online
```

**PDF Display Components:**

**Android:**
- **PaperDetailsActivity**: Activity chính để hiển thị paper details
- **PDF Viewer**: Sử dụng thư viện như AndroidPdfViewer
- **Cache Strategy**: 
  - Download PDF từ S3 URL
  - Lưu vào local storage (Room database hoặc file system)
  - Load từ cache khi offline

**Web:**
- **PDF Viewer**: Sử dụng PDF.js hoặc iframe
- **Direct URL Access**: S3 URL có thể được access trực tiếp vì ACL là PUBLIC_READ

**S3 URL Format:**
```
https://{bucket-name}.s3.{region}.amazonaws.com/papers/{uuid}.pdf
```

**Ví dụ:**
```
https://labverse-papers.s3.ap-southeast-1.amazonaws.com/papers/550e8400-e29b-41d4-a716-446655440000.pdf
```

**Security:**
- S3 bucket có ACL PUBLIC_READ cho phép public access
- URLs có thể được share và access trực tiếp
- Không cần authentication để download PDF từ S3
- Paper metadata vẫn được protect qua API authentication

**Performance:**
- S3 CDN: Sử dụng CloudFront (nếu configure) để cache và accelerate
- Local Caching: Android app cache PDFs locally để offline access
- Lazy Loading: PDF chỉ được download khi user click để xem

---

## Tóm Tắt

### Team Management
- ✅ Đã ánh xạ đầy đủ với Core Functional.md
- ✅ Bổ sung thêm role hierarchy, privacy settings, research field
- ✅ Chi tiết hóa member management và permissions

### Reading List Management
- ✅ Đã ánh xạ đầy đủ với Core Functional.md
- ✅ Implement đầy đủ CRUD operations
- ✅ Hỗ trợ collaborative management với equal permissions

### PDF Upload và Display
- ✅ Upload PDF lên S3 với public-read ACL
- ✅ Lưu S3 URL trong database
- ✅ Hiển thị PDF từ S3 URL trong app
- ✅ Hỗ trợ offline access với local caching

---

**Tài liệu này được tạo để mô tả chi tiết các luồng hoạt động và đảm bảo tính nhất quán giữa Use Case Specification và Core Functional.md.**

