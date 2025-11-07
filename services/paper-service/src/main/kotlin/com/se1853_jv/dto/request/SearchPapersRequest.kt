package com.se1853_jv.dto.request

import jakarta.validation.constraints.Min

data class SearchPapersRequest(
    // Full-text search fields
    val query: String? = null, // General search across title, authors, journal, keywords
    
    // Specific filters
    val title: String? = null,
    val authors: String? = null,
    val journal: String? = null,
    val keywords: List<String>? = null,
    val doi: String? = null,
    
    // Year range filter
    val yearFrom: Int? = null,
    val yearTo: Int? = null,
    
    // Tags filter
    val tagIds: List<String>? = null,
    
    // Pagination
    @field:Min(0, message = "Page index must be >= 0")
    val pageIndex: Int = 0,
    
    @field:Min(1, message = "Page size must be >= 1")
    val pageSize: Int = 10,
    
    // Sort options
    val sortBy: String? = null, // "title", "publicationYear", "authors"
    val sortOrder: String? = "asc" // "asc" or "desc"
)

