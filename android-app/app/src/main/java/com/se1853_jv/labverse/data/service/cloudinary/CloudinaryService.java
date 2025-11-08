package com.se1853_jv.labverse.data.service.cloudinary;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.se1853_jv.labverse.data.utils.CloudinaryStorageHelper;

/**
 * Service để upload PDF files lên Cloudinary
 * Thay thế cho FirebaseService
 */
public class CloudinaryService {
    private final CloudinaryStorageHelper storageHelper;
    
    public CloudinaryService() {
        this.storageHelper = new CloudinaryStorageHelper();
    }
    
    /**
     * Upload PDF file lên Cloudinary
     * @param context Context của app (cần thiết cho content:// URI)
     * @param pdfUri URI của PDF file
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfToCloudinary(Context context, Uri pdfUri, @NonNull UploadCallback callback) {
        if (pdfUri == null) {
            callback.onFailure(new Exception("PDF URI is null"));
            return;
        }
        
        storageHelper.uploadPdfFile(context, pdfUri, new CloudinaryStorageHelper.StorageUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                callback.onSuccess(downloadUrl);
            }
            
            @Override
            public void onError(String error) {
                callback.onFailure(new Exception(error));
            }
            
            @Override
            public void onProgress(int progress) {
                // Progress callback if needed
            }
        });
    }
    
    /**
     * Interface để nhận kết quả upload
     */
    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }
}

