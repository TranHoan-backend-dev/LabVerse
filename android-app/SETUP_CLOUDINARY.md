# Hướng Dẫn Setup Cloudinary cho Android App

## Tổng Quan
Cloudinary là một dịch vụ cloud miễn phí để lưu trữ và quản lý media files (images, videos, PDFs, etc.). 
Thay thế Firebase Storage để tránh chi phí.

## Bước 1: Tạo Tài Khoản Cloudinary

### 1.1. Đăng ký tài khoản
1. Truy cập: https://cloudinary.com/
2. Click **"Sign Up for Free"**
3. Điền thông tin và tạo tài khoản
4. Xác nhận email

### 1.2. Lấy thông tin API
Sau khi đăng nhập, vào **Dashboard**:
- **Cloud name**: Tên cloud của bạn (ví dụ: `demo`)
- **API Key**: Key để authenticate
- **API Secret**: Secret key (giữ bí mật!)

## Bước 2: Cấu Hình Cloudinary trong App

### 2.1. Thêm Credentials vào local.properties

Mở file `local.properties` (ở root của project) và thêm:

```properties
# Cloudinary Configuration
cloudinary.cloud.name=your_cloud_name
cloudinary.api.key=your_api_key
cloudinary.api.secret=your_api_secret
```

**Lưu ý:** File `local.properties` đã có trong `.gitignore`, nên sẽ không bị commit lên Git.

### 2.2. BuildConfig đã được cấu hình tự động

File `build.gradle.kts` đã được cấu hình để:
- Đọc credentials từ `local.properties`
- Inject vào `BuildConfig` khi build
- `LabVerseApplication` sẽ tự động khởi tạo Cloudinary khi app khởi động

### 2.3. Application Class đã được đăng ký

`LabVerseApplication` đã được đăng ký trong `AndroidManifest.xml` và sẽ tự động khởi tạo Cloudinary khi app khởi động.

**Không cần làm gì thêm!** Chỉ cần đảm bảo `local.properties` có đầy đủ credentials.

## Bước 3: Sử Dụng CloudinaryStorageHelper

### 3.1. Upload PDF File

```java
import com.se1853_jv.labverse.data.utils.CloudinaryStorageHelper;

CloudinaryStorageHelper storageHelper = new CloudinaryStorageHelper();

// Upload từ URI
storageHelper.uploadPdfFile(fileUri, new CloudinaryStorageHelper.UploadCallback() {
    @Override
    public void onSuccess(String downloadUrl) {
        // URL của file đã upload
        Log.d(TAG, "File URL: " + downloadUrl);
    }
    
    @Override
    public void onError(String error) {
        Log.e(TAG, "Upload failed: " + error);
    }
    
    @Override
    public void onProgress(int progress) {
        // Progress: 0-100
        Log.d(TAG, "Progress: " + progress + "%");
    }
});

// Upload từ File object
File pdfFile = new File(filePath);
storageHelper.uploadPdfFile(pdfFile, callback);
```

### 3.2. Sử dụng CloudinaryService (Wrapper)

```java
import com.se1853_jv.labverse.data.service.cloudinary.CloudinaryService;

CloudinaryService cloudinaryService = new CloudinaryService();

cloudinaryService.uploadPdfToCloudinary(uri, new CloudinaryService.UploadCallback() {
    @Override
    public void onSuccess(String downloadUrl) {
        // Success
    }
    
    @Override
    public void onFailure(Exception e) {
        // Error
    }
});
```

## Bước 4: Kiểm Tra Upload

### 4.1. Kiểm tra trên Cloudinary Console
1. Đăng nhập vào: https://console.cloudinary.com/
2. Vào **Media Library**
3. Tìm folder `papers/` để xem các file PDF đã upload

### 4.2. Kiểm tra Logs
- Tag: `CloudinaryStorageHelper`
- Xem progress và URL trong Logcat

## Bước 5: Cloudinary Storage Structure

### Folder Structure
```
papers/
  ├── {uuid1}.pdf
  ├── {uuid2}.pdf
  └── ...
```

### URL Format
- **Secure URL**: `https://res.cloudinary.com/{cloud_name}/raw/upload/v{version}/papers/{public_id}.pdf`
- **Public URL**: `http://res.cloudinary.com/{cloud_name}/raw/upload/v{version}/papers/{public_id}.pdf`

## Lưu Ý Quan Trọng

### Giới Hạn Free Plan
- **Storage**: 25 GB
- **Bandwidth**: 25 GB/tháng
- **Transformations**: Unlimited
- **File size**: Max 10 MB/file (có thể upgrade)

### Security Best Practices
1. **KHÔNG** commit API Secret vào Git
2. Sử dụng environment variables hoặc secure storage
3. Giới hạn API Secret chỉ cho upload operations
4. Sử dụng **Signed Uploads** cho production (tùy chọn)

### Upload Settings
- **Resource Type**: `raw` (cho PDF files)
- **Folder**: `papers/` (tự động tạo)
- **Public ID**: UUID (tự động generate)

## Troubleshooting

### Lỗi: "Cloudinary not initialized"
- Đảm bảo đã gọi `CloudinaryStorageHelper.init()` trước khi upload
- Kiểm tra Application class đã được đăng ký trong AndroidManifest.xml

### Lỗi: "Invalid credentials"
- Kiểm tra lại Cloud name, API Key, API Secret
- Đảm bảo không có khoảng trắng thừa

### Upload chậm
- Kiểm tra kết nối internet
- File size quá lớn (>10MB) có thể cần upgrade plan

### File không xuất hiện trên Console
- Đợi vài giây để sync
- Refresh Media Library
- Kiểm tra folder `papers/` trong Console

## Tài Liệu Tham Khảo

- Cloudinary Android SDK: https://cloudinary.com/documentation/android_integration
- Cloudinary Console: https://console.cloudinary.com/
- API Reference: https://cloudinary.com/documentation/image_upload_api_reference

## Checklist

- [ ] Tài khoản Cloudinary đã được tạo
- [ ] Cloud name, API Key, API Secret đã được lấy
- [ ] Application class đã được tạo và đăng ký
- [ ] CloudinaryStorageHelper.init() đã được gọi
- [ ] Test upload thành công
- [ ] File xuất hiện trên Cloudinary Console

