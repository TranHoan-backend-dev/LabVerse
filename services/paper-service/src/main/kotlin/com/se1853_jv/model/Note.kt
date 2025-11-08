package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.UUID

/**
 * Note entity - Lưu annotations dạng text của user trên PDF
 * MongoDB document - Đơn giản hóa, không cần junction tables
 */
@Document(collection = "notes")
data class Note(
    @Id
    val id: String = UUID.randomUUID().toString(),

    val paperId: String,
    val collectionId: String,
    val userId: String,
    val content: String,
    val coordinationX: Int,
    val coordinationY: Int,
    val pageNumber: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)







