package com.se1853_jv.config;


import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Cấu hình Firebase để khởi tạo FirebaseApp và cung cấp đối tượng Firestore.
 * Sẽ tự động chạy @PostConstruct để khởi tạo FirebaseApp khi ứng dụng khởi động.
 */
@Slf4j
@Configuration
public class FirebaseConfig {


    // Địa chỉ Database URL, chỉ cần thiết nếu bạn dùng Realtime Database,
    // nhưng tốt nhất nên giữ lại trong cấu hình FirebaseOptions
    private static final String DATABASE_URL = "https://labverse-18297-default-rtdb.firebaseio.com";

    // Đường dẫn tới file service account key (trong thư mục resources)
    private static final String RESOURCE = "/labverse-18297-firebase-adminsdk-fbsvc-77278ce38c.json";

    /**
     * Phương thức khởi tạo FirebaseApp.
     * @PostConstruct đảm bảo phương thức này chạy sau khi FirebaseConfig được khởi tạo.
     */
    @PostConstruct
    public void initializeFirebase() {
        try {
            // Lấy InputStream từ file service account trong thư mục resources
            // Dùng getClass().getResourceAsStream() để đảm bảo tìm thấy file dù ứng dụng chạy ở đâu
            InputStream serviceAccount = getClass().getResourceAsStream(RESOURCE);

            if (serviceAccount == null) {
                log.error("🚫 Không tìm thấy file service account: {}", RESOURCE);
                throw new IOException("Service account file not found in resources.");
            }

            // Xây dựng FirebaseOptions
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(DATABASE_URL)
                    .build();

            // Chỉ khởi tạo FirebaseApp nếu chưa có ứng dụng nào được khởi tạo
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("🔥 FirebaseApp đã được khởi tạo thành công!");
            }

        } catch (IOException e) {
            log.error("❌ Lỗi khi khởi tạo Firebase: {}", e.getMessage(), e);
            // Có thể re-throw RuntimeException để ngăn ứng dụng khởi động nếu cấu hình Firebase thất bại
            throw new RuntimeException("Failed to initialize Firebase services.", e);
        }
    }

    /**
     * Cung cấp đối tượng Firestore là một Spring Bean.
     * Bean này có thể được inject (autowired) vào các service khác.
     * @return Đối tượng Firestore để tương tác với cơ sở dữ liệu.
     */
    @Bean
    public Firestore getFirestore() {
        // Lấy Firestore client từ FirebaseApp đã được khởi tạo
        return FirestoreClient.getFirestore();
    }
}
