package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "favorites")
data class Favorite(
    @Id
    val id: String? = null,
    val paperId: String,
    val userId: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

