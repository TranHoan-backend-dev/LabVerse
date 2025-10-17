package com.se1853_jv.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import javax.annotation.PostConstruct

private val logger = KotlinLogging.logger {}

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun config() {
        val serviceAccount =
            this::class.java.getResourceAsStream("/labverse-18297-firebase-adminsdk-fbsvc-77278ce38c.json")
//        val serviceAccount: FileInputStream =
//            FileInputStream("D:\\FPTU\\Du_an_ca_nhan\\LabVerse\\services\\paper-service\\src\\main\\resources\\labverse-18297-firebase-adminsdk-fbsvc-77278ce38c.json")

        logger.info { "${serviceAccount?.available()}" }

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        logger.info { "Credentials: ${options.projectId}" }

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
            logger.info { "🔥 FirebaseApp has been initialized!" }
        }
    }

    @Bean
    fun getFirebase(): Firestore {
        return FirestoreClient.getFirestore()
    }
}