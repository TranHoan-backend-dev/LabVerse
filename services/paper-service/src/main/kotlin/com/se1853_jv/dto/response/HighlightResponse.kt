package com.se1853_jv.dto.response

import java.util.UUID

data class HighlightResponse(
    val id: String,
    val color: String,
    val coordinationX: Long,
    val coordinationY: Long,
    val pageNumber: Int,
    val paperId: UUID,
    val collectionId: UUID,
    val userId: UUID
)







