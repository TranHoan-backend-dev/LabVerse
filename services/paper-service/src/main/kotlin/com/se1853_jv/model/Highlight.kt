package com.se1853_jv.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "highlights")
data class Highlight(
    @Id
    val id: String? = null,
    val colorCode: String? = null,
    val annotation: Annotation? = null,
)
