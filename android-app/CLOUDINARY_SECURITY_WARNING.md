# ⚠️ CẢNH BÁO BẢO MẬT - Cloudinary Credentials

## Vấn Đề
File `LabVerseApplication.java` hiện đang chứa **API Secret** của Cloudinary được hardcode trực tiếp trong source code.

## Rủi Ro
- **API Secret** có thể bị lộ nếu commit code lên Git repository công khai
- Người khác có thể sử dụng credentials của bạn để upload files
- Có thể gây tốn chi phí nếu bị lạm dụng

## Giải Pháp Khuyến Nghị

### Option 1: Sử dụng BuildConfig (Cho Development)
1. Thêm vào `build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"your_cloud_name\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"your_api_key\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"your_api_secret\"")
    }
}
```

**⚠️ LƯU Ý:** Thay thế các giá trị placeholder bằng credentials thực tế từ Cloudinary Dashboard.

2. Sử dụng trong code:
```java
CloudinaryStorageHelper.init(
    this,
    BuildConfig.CLOUDINARY_CLOUD_NAME,
    BuildConfig.CLOUDINARY_API_KEY,
    BuildConfig.CLOUDINARY_API_SECRET
);
```

### Option 2: Sử dụng local.properties (Khuyến nghị cho Development)
1. Thêm vào `local.properties`:
```properties
cloudinary.cloud.name=your_cloud_name
cloudinary.api.key=your_api_key
cloudinary.api.secret=your_api_secret
```

**⚠️ LƯU Ý:** Thay thế các giá trị placeholder bằng credentials thực tế từ Cloudinary Dashboard.

2. Đảm bảo `local.properties` đã có trong `.gitignore` (đã có sẵn)

3. Đọc trong code:
```java
Properties properties = new Properties();
try {
    properties.load(new FileInputStream("local.properties"));
    String cloudName = properties.getProperty("cloudinary.cloud.name");
    String apiKey = properties.getProperty("cloudinary.api.key");
    String apiSecret = properties.getProperty("cloudinary.api.secret");
    CloudinaryStorageHelper.init(this, cloudName, apiKey, apiSecret);
} catch (IOException e) {
    Log.e(TAG, "Error loading Cloudinary config", e);
}
```

### Option 3: Server-side Upload (Cho Production - Khuyến nghị nhất)
- Tạo API endpoint trên backend để generate signed upload URLs
- Android app chỉ cần gọi API để lấy signed URL
- API Secret chỉ tồn tại trên server, không bao giờ được gửi đến client

## Hành Động Ngay Lập Tức

### Nếu đã commit code lên Git:
1. **Đổi API Secret ngay lập tức** trên Cloudinary Console:
   - Vào: https://console.cloudinary.com/
   - Settings → Security → API Keys
   - Click "Regenerate" cho API Secret

2. Xóa commit chứa credentials:
   ```bash
   git rebase -i HEAD~n  # n = số commits cần xóa
   # Hoặc force push sau khi đã xóa credentials
   ```

3. Thêm `LabVerseApplication.java` vào `.gitignore` nếu chứa credentials

### Nếu chưa commit:
- **KHÔNG commit** file `LabVerseApplication.java` nếu nó chứa API Secret
- Sử dụng một trong các giải pháp trên để bảo mật credentials

## Kiểm Tra
- [ ] API Secret đã được đổi sau khi commit lên Git công khai
- [ ] Credentials không còn trong Git history
- [ ] Đã áp dụng một trong các giải pháp bảo mật trên
- [ ] `.gitignore` đã được cập nhật nếu cần

## Tài Liệu Tham Khảo
- Cloudinary Security Best Practices: https://cloudinary.com/documentation/security
- Android Secrets Management: https://developer.android.com/training/articles/keystore

