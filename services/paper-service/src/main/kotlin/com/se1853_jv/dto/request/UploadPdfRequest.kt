package com.se1853_jv.dto.request

import com.se1853_jv.config.annotation.CurrentYear
import jakarta.validation.constraints.Pattern

data class UploadPdfRequest(
    @field:Pattern(
        regexp = "^(https?://)?([\\w\\-]+\\.)+[\\w\\-]+(/[\\w\\-.,@?^=%&:/~+#]*)?$",
        message = "URI must follow the format"
    )
    val dataUrl: String,
    val description: String?,
    val keywords: List<String>?,
    val title: String,
    val authors: String,
    val journal: String,

    @CurrentYear
    val publicationYear: Int,

    @field:Pattern(
        regexp = "(?i)^(?:doi:\\s*|https?://(?:dx\\.)?doi\\.org/)?(10\\.\\d{4,9}/[-._;()/:A-Z0-9]+)$",
        message = "DOI must follow the correct format"
    )
    val doi: String,
    val tags: List<String>?,
)
