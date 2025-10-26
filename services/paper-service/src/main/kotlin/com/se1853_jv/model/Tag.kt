package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "tags")
data class Tag(
    @Id
    val id: String? = null,
    val name: String? = null,
    val paperIds: List<String> = emptyList(),
)
