package com.se1853_jv.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct

private val logger = KotlinLogging.logger {}
private const val DATABASE_URL: String = "https://labverse-18297-default-rtdb.asia-southeast1.firebasedatabase.app"
private const val RESOURCE: String = "/labverse-18297-firebase-adminsdk-fbsvc-77278ce38c.json"

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun config() {
        try {
            val serviceAccount = this::class.java.getResourceAsStream(RESOURCE)
            
            if (serviceAccount == null) {
                logger.warn { "⚠️ Firebase credentials file not found at $RESOURCE. Firebase will not be initialized." }
                logger.warn { "⚠️ To enable Firebase, place the credentials JSON file in src/main/resources/" }
                return
            }

            val options: FirebaseOptions = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(DATABASE_URL)
                .build()

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options)
                logger.info { "🔥 FirebaseApp has been initialized!" }
            }
        } catch (e: Exception) {
            logger.error(e) { "❌ Failed to initialize Firebase: ${e.message}" }
            logger.warn { "⚠️ Application will continue without Firebase. Some features may not work." }
        }
    }

    @Bean
    fun getFirebase(): Firestore? {
        return try {
            if (FirebaseApp.getApps().isEmpty()) {
                logger.warn { "⚠️ Firebase not initialized. Firestore bean will return null." }
                null
            } else {
                FirestoreClient.getFirestore()
            }
        } catch (e: Exception) {
            logger.error(e) { "❌ Failed to create Firestore bean: ${e.message}" }
            null
        }
    }
}