package com.se1853_jv.dto.response

data class PaperResponse(
    val id: String,
    val dataUrl: String,
    val keywords: List<String>? = null,
    val title: String,
    val authors: String,
    val journal: String,
    val publicationYear: Int,
    val doi: String,
    val description: String?,
)
