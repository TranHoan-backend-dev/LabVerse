package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

/**
 * Highlight entity - Lưu highlights (màu sắc) của user trên PDF
 */
@Document(collection = "highlights")
data class Highlight(
    @Id
    val id: String = UUID.randomUUID().toString(),

    val paperId: String,
    val collectionId: String,
    val userId: String,
    val color: String,
    val coordinationX: Int,
    val coordinationY: Int,
    val pageNumber: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)







