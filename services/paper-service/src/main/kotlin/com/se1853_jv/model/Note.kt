package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "notes")
data class Note(
    @Id
    val id: String? = null,
    val content: String? = null,
) : Annotation()
