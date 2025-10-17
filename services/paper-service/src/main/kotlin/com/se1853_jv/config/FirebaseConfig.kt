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
private const val DATABASE_URL: String = "https://console.firebase.google.com/u/0/project/labverse-18297/database/labverse-18297-default-rtdb/data/~2F"
private const val RESOURCE: String = "/labverse-18297-firebase-adminsdk-fbsvc-77278ce38c.json"

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun config() {
        val serviceAccount =
            this::class.java.getResourceAsStream(RESOURCE)

        val options: FirebaseOptions = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .setDatabaseUrl(DATABASE_URL)
            .build()

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