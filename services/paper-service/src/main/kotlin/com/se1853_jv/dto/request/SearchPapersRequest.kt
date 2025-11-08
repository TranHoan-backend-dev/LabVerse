package com.se1853_jv.dto.request

import com.se1853_jv.config.annotation.CurrentYear
import com.se1853_jv.config.annotation.ValidYearRange
import jakarta.validation.constraints.*

@ValidYearRange
data class SearchPapersRequest(
    // Full-text search fields
    val query: String? = null, // General search across title, authors, journal, keywords
    
    // Specific filters
    val title: String? = null,
    val authors: String? = null,
    val journal: String? = null,
    
    val keywords: List<String>? = null,
    
    @field:Pattern(
        regexp = "(?i)^(?:doi:\\s*|https?://(?:dx\\.)?doi\\.org/)?(10\\.\\d{4,9}/[-._;()/:A-Z0-9]+)$",
        message = "DOI must follow the correct format"
    )
    val doi: String? = null,
    
    // Year range filter
    @field:Min(value = 0, message = "Year from must be >= 0")
    @CurrentYear(message = "Year from cannot be in the future")
    val yearFrom: Int? = null,
    
    @field:Min(value = 0, message = "Year to must be >= 0")
    @CurrentYear(message = "Year to cannot be in the future")
    val yearTo: Int? = null,
    
    // Tags filter
    val tagIds: List<String>? = null,
    
    // Pagination
    @field:Min(value = 0, message = "Page index must be >= 0")
    val pageIndex: Int = 0,
    
    @field:Min(value = 1, message = "Page size must be >= 1")
    @field:Max(value = 100, message = "Page size must be <= 100")
    val pageSize: Int = 10,
    
    // Sort options
    @field:Pattern(
        regexp = "^(title|publicationYear|year|authors|journal)?$",
        flags = [Pattern.Flag.CASE_INSENSITIVE],
        message = "Sort by must be one of: title, publicationYear, year, authors, journal"
    )
    val sortBy: String? = null, // "title", "publicationYear", "authors", "journal"
    
    @field:Pattern(
        regexp = "^(asc|desc)$",
        flags = [Pattern.Flag.CASE_INSENSITIVE],
        message = "Sort order must be 'asc' or 'desc'"
    )
    val sortOrder: String? = "asc" // "asc" or "desc"
)

