package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "papers")
data class Paper(
    @Id
    val id: String? = null,
    val dataUrl: String? = null,
    val keywords: List<String>? = emptyList(),
    val citationIds: List<String> = emptyList(),
    val tagIds: List<String> = emptyList(),
    ) : Metadata()
