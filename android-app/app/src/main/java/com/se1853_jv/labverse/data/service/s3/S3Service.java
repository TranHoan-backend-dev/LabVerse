package com.se1853_jv.labverse.data.service.s3;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.se1853_jv.labverse.data.utils.S3StorageHelper;

/**
 * Service để upload PDF files lên AWS S3
 */
public class S3Service {
    private final S3StorageHelper storageHelper;
    
    public S3Service() {
        this.storageHelper = new S3StorageHelper();
    }
    
    /**
     * Upload PDF file lên S3
     * @param context Context của app (cần thiết cho content:// URI)
     * @param pdfUri URI của PDF file
     * @param callback Callback để nhận kết quả
     */
    public void uploadPdfToS3(Context context, Uri pdfUri, @NonNull UploadCallback callback) {
        if (pdfUri == null) {
            callback.onFailure(new Exception("PDF URI is null"));
            return;
        }
        
        storageHelper.uploadPdfFile(context, pdfUri, new S3StorageHelper.StorageUploadCallback() {
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

