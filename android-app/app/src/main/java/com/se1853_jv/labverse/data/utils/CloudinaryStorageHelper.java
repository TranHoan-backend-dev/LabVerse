package com.se1853_jv.labverse.data.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Helper class để upload files lên Cloudinary
 */
public class CloudinaryStorageHelper {
    private static final String TAG = "CloudinaryStorageHelper";
    private static final String PAPERS_FOLDER = "papers";
    private static boolean isInitialized = false;
    
    /**
     * Khởi tạo Cloudinary với config
     * Cần gọi method này trước khi upload file
     * @param context Context của app
     * @param cloudName Cloud name từ Cloudinary dashboard
     * @param apiKey API Key từ Cloudinary dashboard
     * @param apiSecret API Secret từ Cloudinary dashboard
     */
    public static void init(Context context, String cloudName, String apiKey, String apiSecret) {
        if (isInitialized) {
            Log.d(TAG, "Cloudinary already initialized");
            return;
        }
        
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            
            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "✅ Cloudinary initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Cloudinary: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Cloudinary", e);
        }
    }
    
    /**
     * Upload PDF file lên Cloudinary
     * @param fileUri URI của file PDF (có thể là file:// hoặc content://)
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfFile(Uri fileUri, StorageUploadCallback callback) {
        uploadPdfFile(null, fileUri, callback);
    }
    
    /**
     * Upload PDF file lên Cloudinary với context (cần thiết cho content:// URI)
     * @param context Context của app (cần thiết để đọc content:// URI)
     * @param fileUri URI của file PDF
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfFile(Context context, Uri fileUri, StorageUploadCallback callback) {
        if (fileUri == null) {
            callback.onError("File URI is null");
            return;
        }
        
        if (!isInitialized) {
            callback.onError("Cloudinary not initialized. Call CloudinaryStorageHelper.init() first");
            return;
        }
        
        // Kiểm tra nếu là content:// URI, cần copy vào temporary file
        String uriScheme = fileUri.getScheme();
        if ("content".equals(uriScheme)) {
            if (context == null) {
                callback.onError("Context is required for content:// URI");
                return;
            }
            uploadContentUri(context, fileUri, callback);
        } else if ("file".equals(uriScheme)) {
            // File URI - upload trực tiếp
            uploadFileUri(fileUri, callback);
        } else {
            callback.onError("Unsupported URI scheme: " + uriScheme);
        }
    }
    
    /**
     * Upload file từ content:// URI bằng cách copy vào temporary file
     */
    private void uploadContentUri(Context context, Uri contentUri, StorageUploadCallback callback) {
        File tempFile = null;
        try {
            // Tạo temporary file
            tempFile = new File(context.getCacheDir(), "upload_" + UUID.randomUUID().toString() + ".pdf");
            
            Log.d(TAG, "📋 Copying content:// URI to temporary file...");
            Log.d(TAG, "📎 Source URI: " + contentUri.toString());
            Log.d(TAG, "📁 Temp file: " + tempFile.getAbsolutePath());
            
            // Copy file từ content URI vào temporary file
            ContentResolver resolver = context.getContentResolver();
            InputStream inputStream = resolver.openInputStream(contentUri);
            if (inputStream == null) {
                callback.onError("Cannot open input stream from URI");
                return;
            }
            
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            outputStream.close();
            inputStream.close();
            
            Log.d(TAG, "✅ File copied successfully. Size: " + (totalBytes / 1024) + " KB");
            
            // Upload temporary file
            uploadFileUri(Uri.fromFile(tempFile), callback);
            
        } catch (IOException e) {
            Log.e(TAG, "Error copying file: " + e.getMessage(), e);
            callback.onError("Error copying file: " + e.getMessage());
        } finally {
            // Cleanup: xóa temporary file sau khi upload xong (hoặc lỗi)
            // Note: File sẽ được xóa sau khi upload thành công trong callback
        }
    }
    
    /**
     * Upload file từ file:// URI
     */
    private void uploadFileUri(Uri fileUri, StorageUploadCallback callback) {
        try {
            // Tạo unique filename
            String fileName = PAPERS_FOLDER + "/" + UUID.randomUUID().toString() + ".pdf";
            
            Log.d(TAG, "🚀 Starting upload...");
            Log.d(TAG, "📁 File path: " + fileName);
            Log.d(TAG, "📎 File URI: " + fileUri.toString());
            
            // Lấy file path từ URI
            String filePath = fileUri.getPath();
            if (filePath == null) {
                callback.onError("Cannot get file path from URI");
                return;
            }
            
            // Upload file với resource_type = "raw" cho PDF files
            MediaManager.get().upload(filePath)
                    .option("resource_type", "raw")
                    .option("public_id", fileName.replace(".pdf", "")) // Remove extension for public_id
                    .option("folder", PAPERS_FOLDER)
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "📤 Upload started: " + requestId);
                            callback.onProgress(0);
                        }
                        
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            Log.d(TAG, "Upload progress: " + progress + "%");
                            callback.onProgress(progress);
                        }
                        
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String downloadUrl = resultData.get("secure_url") != null 
                                    ? resultData.get("secure_url").toString() 
                                    : resultData.get("url").toString();
                            Log.d(TAG, "✅ File uploaded successfully!");
                            Log.d(TAG, "📎 Download URL: " + downloadUrl);
                            Log.d(TAG, "📁 File path in Cloudinary: " + fileName);
                            Log.d(TAG, "💡 Check Cloudinary Console → Media Library → papers/ to verify");
                            
                            // Cleanup temporary file nếu có
                            cleanupTempFile(fileUri);
                            
                            callback.onSuccess(downloadUrl);
                        }
                        
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            String errorMessage = "Failed to upload file: " + error.getDescription();
                            Log.e(TAG, "Error uploading file: " + errorMessage);
                            
                            // Cleanup temporary file nếu có
                            cleanupTempFile(fileUri);
                            
                            callback.onError(errorMessage);
                        }
                        
                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                            // Retry upload (không cleanup file vì sẽ cần lại)
                            uploadFileUri(fileUri, callback);
                        }
                    })
                    .dispatch();
                    
        } catch (Exception e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage(), e);
            cleanupTempFile(fileUri);
            callback.onError("Error uploading file: " + e.getMessage());
        }
    }
    
    /**
     * Cleanup temporary file nếu là file trong cache
     */
    private void cleanupTempFile(Uri fileUri) {
        try {
            String path = fileUri.getPath();
            if (path != null && path.contains("upload_")) {
                File tempFile = new File(path);
                if (tempFile.exists() && tempFile.delete()) {
                    Log.d(TAG, "🗑️ Cleaned up temporary file: " + path);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Error cleaning up temp file: " + e.getMessage());
        }
    }
    
    /**
     * Upload PDF file từ File object
     * @param file File PDF
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfFile(File file, StorageUploadCallback callback) {
        if (file == null || !file.exists()) {
            callback.onError("File is null or does not exist");
            return;
        }
        
        if (!isInitialized) {
            callback.onError("Cloudinary not initialized. Call CloudinaryStorageHelper.init() first");
            return;
        }
        
        try {
            // Tạo unique filename
            String fileName = PAPERS_FOLDER + "/" + UUID.randomUUID().toString() + ".pdf";
            
            Log.d(TAG, "🚀 Starting upload from File...");
            Log.d(TAG, "📁 File path: " + fileName);
            Log.d(TAG, "📄 File name: " + file.getName());
            Log.d(TAG, "📏 File size: " + (file.length() / 1024) + " KB");
            
            // Upload file với resource_type = "raw" cho PDF files
            MediaManager.get().upload(file.getAbsolutePath())
                    .option("resource_type", "raw")
                    .option("public_id", fileName.replace(".pdf", "")) // Remove extension for public_id
                    .option("folder", PAPERS_FOLDER)
                    .callback(new com.cloudinary.android.callback.UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Log.d(TAG, "📤 Upload started: " + requestId);
                            callback.onProgress(0);
                        }
                        
                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                            int progress = (int) ((bytes * 100) / totalBytes);
                            Log.d(TAG, "Upload progress: " + progress + "%");
                            callback.onProgress(progress);
                        }
                        
                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String downloadUrl = resultData.get("secure_url") != null 
                                    ? resultData.get("secure_url").toString() 
                                    : resultData.get("url").toString();
                            Log.d(TAG, "✅ File uploaded successfully!");
                            Log.d(TAG, "📎 Download URL: " + downloadUrl);
                            Log.d(TAG, "📁 File path in Cloudinary: " + fileName);
                            Log.d(TAG, "💡 Check Cloudinary Console → Media Library → papers/ to verify");
                            callback.onSuccess(downloadUrl);
                        }
                        
                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            String errorMessage = "Failed to upload file: " + error.getDescription();
                            Log.e(TAG, "Error uploading file: " + errorMessage);
                            callback.onError(errorMessage);
                        }
                        
                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                            Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                            // Retry upload
                            uploadPdfFile(file, callback);
                        }
                    })
                    .dispatch();
                    
        } catch (Exception e) {
            Log.e(TAG, "Error uploading file: " + e.getMessage());
            callback.onError("Error uploading file: " + e.getMessage());
        }
    }
    
    /**
     * Interface để nhận kết quả upload
     */
    public interface StorageUploadCallback {
        void onSuccess(String downloadUrl);
        void onError(String error);
        void onProgress(int progress);
    }
}

