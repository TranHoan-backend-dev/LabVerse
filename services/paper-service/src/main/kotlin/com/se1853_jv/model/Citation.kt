package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "citations")
data class Citation(
    @Id
    val id: String,
    val paperIds: List<String> = emptyList(),
) : Metadata()
