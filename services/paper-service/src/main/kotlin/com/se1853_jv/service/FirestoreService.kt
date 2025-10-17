package com.se1853_jv.service

import com.google.api.core.ApiFuture
import com.google.cloud.firestore.DocumentReference
import com.google.cloud.firestore.Firestore
import com.google.cloud.firestore.QuerySnapshot
import com.google.firebase.cloud.FirestoreClient
import org.springframework.stereotype.Service
import java.util.concurrent.ExecutionException

@Service
class FirestoreService(
    private val db: Firestore = FirestoreClient.getFirestore()
) {
    @Throws(ExecutionException::class, InterruptedException::class)
    fun saveUser(name: String, email: String, role: String): String {
        val user: MutableMap<String, Any> = HashMap()
        user["name"] = name
        user["email"] = email
        user["role"] = role

        val addedDocRef: ApiFuture<DocumentReference> = db.collection("users").add(user)
        return "Added document with ID: " + addedDocRef.get().getId()
    }

    @Throws(ExecutionException::class, InterruptedException::class)
    fun getAllUsers() {
        val query: ApiFuture<QuerySnapshot> = db.collection("users").get()
        for (doc in query.get().documents) {
            println(doc.id + " => " + doc.data)
        }
    }
}