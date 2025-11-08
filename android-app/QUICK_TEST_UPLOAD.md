# Quick Test Upload - Hướng Dẫn Nhanh

## Cách Nhanh Nhất: Long Press Button "Choose File"

1. **Mở app** trên máy ảo
2. **Vào màn hình Import Paper**
3. **Long press** (giữ lâu) vào button **"Choose File"**
4. App sẽ tự động:
   - Tạo file PDF test
   - Mở dialog để điền thông tin
   - Bạn chỉ cần điền title, authors và click Upload

## Các Cách Khác

### Cách 1: Copy File qua Device File Explorer (Khuyến nghị cho file thật)

1. **Android Studio** → **View** → **Tool Windows** → **Device File Explorer**
2. Navigate đến `/sdcard/Download/`
3. Click **Upload** (icon mũi tên lên)
4. Chọn file PDF từ máy tính
5. Trong app, click **Choose File** → chọn file từ Downloads

### Cách 2: ADB Push (Command Line)

```bash
# Push file vào máy ảo
adb push path/to/your/file.pdf /sdcard/Download/test.pdf

# Sau đó trong app, chọn file từ Downloads
```

### Cách 3: Download từ Internet

Thêm code này vào `ImportPaperManuallyActivity` để download file test:

```java
private void downloadTestPdf() {
    String pdfUrl = "https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf";
    // ... code download
}
```

## Kiểm Tra Upload Thành Công

1. **Xem Logcat**:
   - Tag: `CloudinaryStorageHelper`
   - Tìm dòng: `✅ File uploaded successfully!`
   - Copy URL từ log

2. **Cloudinary Console**:
   - https://console.cloudinary.com/
   - Media Library → papers/
   - File sẽ xuất hiện ở đây

3. **Toast message**:
   - App sẽ hiển thị "Paper uploaded successfully!"

## Troubleshooting

### Long press không hoạt động
- Đảm bảo giữ button ít nhất 1 giây
- Kiểm tra logcat để xem có lỗi không

### Upload failed
- Kiểm tra internet connection
- Kiểm tra Cloudinary credentials trong `local.properties`
- Xem logcat để biết lỗi cụ thể

### File không tạo được
- Kiểm tra permissions
- Xem logcat với tag `TestPdfGenerator`

