package com.se1853_jv.labverse;

import android.app.Application;
import android.util.Log;

import com.se1853_jv.labverse.data.utils.S3StorageHelper;

/**
 * Application class để khởi tạo các services và libraries khi app khởi động
 */
public class LabVerseApplication extends Application {
    private static final String TAG = "LabVerseApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize S3 từ BuildConfig (được inject từ local.properties)
        initializeS3();
    }
    
    /**
     * Khởi tạo S3 bằng cách đọc credentials từ BuildConfig
     * BuildConfig được inject từ local.properties trong build.gradle.kts
     * File local.properties đã có trong .gitignore nên an toàn
     */
    private void initializeS3() {
        try {
            String accessKey = BuildConfig.AWS_ACCESS_KEY;
            String secretKey = BuildConfig.AWS_SECRET_KEY;
            String region = BuildConfig.AWS_REGION;
            String bucket = BuildConfig.AWS_S3_BUCKET;
            
            // Kiểm tra credentials có được set chưa
            if (accessKey == null || accessKey.isEmpty() ||
                secretKey == null || secretKey.isEmpty() ||
                region == null || region.isEmpty() ||
                bucket == null || bucket.isEmpty()) {
                Log.e(TAG, "❌ S3 credentials not found in BuildConfig");
                Log.e(TAG, "Please add the following to local.properties:");
                Log.e(TAG, "aws.access.key=your_access_key");
                Log.e(TAG, "aws.secret.key=your_secret_key");
                Log.e(TAG, "aws.region=your_region");
                Log.e(TAG, "aws.s3.bucket=your_bucket_name");
                Log.e(TAG, "Then rebuild the project.");
                return;
            }
            
            S3StorageHelper.init(this, accessKey, secretKey, region, bucket);
            Log.d(TAG, "✅ S3 initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize S3: " + e.getMessage(), e);
        }
    }
}

