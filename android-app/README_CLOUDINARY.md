# Cloudinary Setup - Quick Guide

## ✅ Đã được cấu hình sẵn

Cloudinary đã được tích hợp vào app với cấu hình bảo mật:

1. ✅ Credentials được lưu trong `local.properties` (không commit lên Git)
2. ✅ `build.gradle.kts` tự động đọc và inject vào `BuildConfig`
3. ✅ `LabVerseApplication` tự động khởi tạo Cloudinary khi app khởi động

## 📝 Cách sử dụng

### 1. Thêm credentials vào local.properties

Mở file `android-app/local.properties` và thêm:

```properties
cloudinary.cloud.name=dfhshcbbi
cloudinary.api.key=891126784452614
cloudinary.api.secret=MreyfnfUdQlDsix37K8gzVoZLmc
```

### 2. Rebuild project

Sau khi thêm credentials, rebuild project để BuildConfig được cập nhật:
- **Android Studio**: Build → Rebuild Project
- **Command line**: `./gradlew clean build`

### 3. Sử dụng trong code

```java
import com.se1853_jv.labverse.data.utils.CloudinaryStorageHelper;

CloudinaryStorageHelper storageHelper = new CloudinaryStorageHelper();

storageHelper.uploadPdfFile(fileUri, new CloudinaryStorageHelper.UploadCallback() {
    @Override
    public void onSuccess(String downloadUrl) {
        // File đã được upload thành công
        Log.d(TAG, "File URL: " + downloadUrl);
    }
    
    @Override
    public void onError(String error) {
        // Xử lý lỗi
        Log.e(TAG, "Upload failed: " + error);
    }
    
    @Override
    public void onProgress(int progress) {
        // Cập nhật progress (0-100)
        Log.d(TAG, "Progress: " + progress + "%");
    }
});
```

## 🔒 Bảo mật

- ✅ `local.properties` đã có trong `.gitignore`
- ✅ Credentials không bao giờ được commit lên Git
- ✅ Mỗi developer có file `local.properties` riêng

## 📚 Tài liệu chi tiết

Xem file `SETUP_CLOUDINARY.md` để biết thêm chi tiết.

