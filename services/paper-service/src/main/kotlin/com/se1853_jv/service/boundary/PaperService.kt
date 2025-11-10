package com.se1853_jv.service.boundary

import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.PaperResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

interface PaperService {
    fun getPaperDetails(paperId: String): PaperResponse
    fun getPapersByUserId(userId: String): List<PaperResponse>
    fun getAllPapers(
        searchQuery: String?, pageIndex: Int, pageSize: Int?,
        author: String?, journal: String?, publicationYearFrom: Int?,
        publicationYearTo: Int?
    ): PageImpl<PaperResponse>

    fun deleteById(id: String)
    fun createNewPaper(req: UploadPdfRequest, userId: String?)
}