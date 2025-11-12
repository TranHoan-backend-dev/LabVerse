package com.se1853_jv.dto.response

class PaginatedPapersListResponse (
    val papers: List<PaperResponse>,
    val totalPages: Int,
    val totalElements: Long
)