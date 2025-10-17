package com.se1853_jv.controller

import com.se1853_jv.service.FirestoreService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.ExecutionException

@RestController
class FirebaseController(
    private val firestoreService: FirestoreService
) {

    @GetMapping("/test/firestore")
    @Throws(ExecutionException::class, InterruptedException::class)
    fun testFirestore(): String {
        firestoreService.saveUser("Cậu", "test@example.com", "developer")
        firestoreService.getAllUsers()
        return "✅ Firestore connection successful!"
    }
}