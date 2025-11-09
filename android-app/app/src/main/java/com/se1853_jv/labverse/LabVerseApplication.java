package com.se1853_jv.labverse;

import android.app.Application;
import android.util.Log;

import com.se1853_jv.labverse.data.utils.CloudinaryStorageHelper;

/**
 * Application class để khởi tạo các services và libraries khi app khởi động
 */
public class LabVerseApplication extends Application {
    private static final String TAG = "LabVerseApplication";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Cloudinary từ BuildConfig (được inject từ local.properties)
        initializeCloudinary();
    }
    
    /**
     * Khởi tạo Cloudinary bằng cách đọc credentials từ BuildConfig
     * BuildConfig được inject từ local.properties trong build.gradle.kts
     * File local.properties đã có trong .gitignore nên an toàn
     */
    private void initializeCloudinary() {
        try {
            String cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME;
            String apiKey = BuildConfig.CLOUDINARY_API_KEY;
            String apiSecret = BuildConfig.CLOUDINARY_API_SECRET;
            
            // Kiểm tra credentials có được set chưa
            if (cloudName == null || cloudName.isEmpty() ||
                apiKey == null || apiKey.isEmpty() ||
                apiSecret == null || apiSecret.isEmpty()) {
                Log.e(TAG, "❌ Cloudinary credentials not found in BuildConfig");
                Log.e(TAG, "Please add the following to local.properties:");
                Log.e(TAG, "cloudinary.cloud.name=your_cloud_name");
                Log.e(TAG, "cloudinary.api.key=your_api_key");
                Log.e(TAG, "cloudinary.api.secret=your_api_secret");
                Log.e(TAG, "Then rebuild the project.");
                return;
            }
            
            CloudinaryStorageHelper.init(this, cloudName, apiKey, apiSecret);
            Log.d(TAG, "✅ Cloudinary initialized successfully");
            
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to initialize Cloudinary: " + e.getMessage(), e);
        }
    }
}

