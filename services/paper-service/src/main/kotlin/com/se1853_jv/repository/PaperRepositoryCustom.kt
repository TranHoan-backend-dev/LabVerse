package com.se1853_jv.repository

import com.se1853_jv.model.Paper
import org.springframework.data.domain.Page

interface PaperRepositoryCustom {
    fun searchPapers(
        searchQuery: String?,
        author: String?,
        journal: String?,
        publicationYearFrom: Int?,
        publicationYearTo: Int?,
        pageIndex: Int,
        pageSize: Int
    ): Page<Paper>
}