package com.se1853_jv.dto.response

import java.util.UUID

data class NoteResponse(
    val id: String,
    val content: String,
    val coordinationX: Long,
    val coordinationY: Long,
    val pageNumber: Int,
    val paperId: UUID,
    val collectionId: UUID,
    val userId: UUID
)







