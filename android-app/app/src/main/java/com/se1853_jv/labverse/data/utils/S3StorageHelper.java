package com.se1853_jv.labverse.data.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Helper class để upload files lên AWS S3
 */
public class S3StorageHelper {
    private static final String TAG = "S3StorageHelper";
    private static final String PAPERS_FOLDER = "papers";
    private static boolean isInitialized = false;
    private static TransferUtility transferUtility;
    private static String bucketName;
    
    /**
     * Khởi tạo S3 với config
     * Cần gọi method này trước khi upload file
     * @param context Context của app
     * @param accessKey AWS Access Key
     * @param secretKey AWS Secret Key
     * @param region AWS Region (e.g., "us-east-1")
     * @param bucket S3 Bucket name
     */
    public static void init(Context context, String accessKey, String secretKey, String region, String bucket) {
        if (isInitialized) {
            Log.d(TAG, "S3 already initialized");
            return;
        }
        
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
            AmazonS3Client s3Client = new AmazonS3Client(credentials, Region.getRegion(Regions.fromName(region)));
            
            transferUtility = TransferUtility.builder()
                    .context(context)
                    .s3Client(s3Client)
                    .build();
            
            bucketName = bucket;
            isInitialized = true;
            Log.d(TAG, "✅ S3 initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing S3: " + e.getMessage());
            throw new RuntimeException("Failed to initialize S3", e);
        }
    }
    
    /**
     * Upload PDF file lên S3
     * @param fileUri URI của file PDF (có thể là file:// hoặc content://)
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfFile(Uri fileUri, StorageUploadCallback callback) {
        uploadPdfFile(null, fileUri, callback);
    }
    
    /**
     * Upload PDF file lên S3 với context (cần thiết cho content:// URI)
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
            callback.onError("S3 not initialized. Call S3StorageHelper.init() first");
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
            
            File file = new File(filePath);
            if (!file.exists()) {
                callback.onError("File does not exist");
                return;
            }
            
            // Tạo ObjectMetadata với ACL public-read
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");
            
            // Upload file lên S3 với ACL public-read
            TransferObserver observer = transferUtility.upload(
                    bucketName,
                    fileName,
                    file,
                    metadata,
                    CannedAccessControlList.PublicRead
            );
            
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        // Tạo download URL
                        String downloadUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
                        Log.d(TAG, "✅ File uploaded successfully!");
                        Log.d(TAG, "📎 Download URL: " + downloadUrl);
                        Log.d(TAG, "📁 File path in S3: " + fileName);
                        
                        // Cleanup temporary file nếu có
                        cleanupTempFile(fileUri);
                        
                        callback.onSuccess(downloadUrl);
                    } else if (state == TransferState.FAILED) {
                        String errorMessage = "Failed to upload file to S3";
                        Log.e(TAG, "Error uploading file: " + errorMessage);
                        
                        // Cleanup temporary file nếu có
                        cleanupTempFile(fileUri);
                        
                        callback.onError(errorMessage);
                    }
                }
                
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int progress = (int) ((bytesCurrent * 100) / bytesTotal);
                    Log.d(TAG, "Upload progress: " + progress + "%");
                    callback.onProgress(progress);
                }
                
                @Override
                public void onError(int id, Exception ex) {
                    String errorMessage = "Failed to upload file: " + ex.getMessage();
                    Log.e(TAG, "Error uploading file: " + errorMessage, ex);
                    
                    // Cleanup temporary file nếu có
                    cleanupTempFile(fileUri);
                    
                    callback.onError(errorMessage);
                }
            });
            
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
            callback.onError("S3 not initialized. Call S3StorageHelper.init() first");
            return;
        }
        
        try {
            // Tạo unique filename
            String fileName = PAPERS_FOLDER + "/" + UUID.randomUUID().toString() + ".pdf";
            
            Log.d(TAG, "🚀 Starting upload from File...");
            Log.d(TAG, "📁 File path: " + fileName);
            Log.d(TAG, "📄 File name: " + file.getName());
            Log.d(TAG, "📏 File size: " + (file.length() / 1024) + " KB");
            
            // Tạo ObjectMetadata với ACL public-read
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("application/pdf");
            
            // Upload file lên S3 với ACL public-read
            TransferObserver observer = transferUtility.upload(
                    bucketName,
                    fileName,
                    file,
                    metadata,
                    CannedAccessControlList.PublicRead
            );
            
            observer.setTransferListener(new TransferListener() {
                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (state == TransferState.COMPLETED) {
                        // Tạo download URL
                        String downloadUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, fileName);
                        Log.d(TAG, "✅ File uploaded successfully!");
                        Log.d(TAG, "📎 Download URL: " + downloadUrl);
                        Log.d(TAG, "📁 File path in S3: " + fileName);
                        callback.onSuccess(downloadUrl);
                    } else if (state == TransferState.FAILED) {
                        String errorMessage = "Failed to upload file to S3";
                        Log.e(TAG, "Error uploading file: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                }
                
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    int progress = (int) ((bytesCurrent * 100) / bytesTotal);
                    Log.d(TAG, "Upload progress: " + progress + "%");
                    callback.onProgress(progress);
                }
                
                @Override
                public void onError(int id, Exception ex) {
                    String errorMessage = "Failed to upload file: " + ex.getMessage();
                    Log.e(TAG, "Error uploading file: " + errorMessage, ex);
                    callback.onError(errorMessage);
                }
            });
            
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

