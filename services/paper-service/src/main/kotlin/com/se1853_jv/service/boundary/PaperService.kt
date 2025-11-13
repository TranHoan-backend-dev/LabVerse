package com.se1853_jv.service.boundary

import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.PaginatedPapersListResponse
import com.se1853_jv.dto.response.PaperResponse

interface PaperService {
    fun getPaperDetails(paperId: String): PaperResponse
    fun getPaperDetails(paperId: String, userId: String?): PaperResponse
    fun getPapersByUserId(userId: String): List<PaperResponse>
    fun getAllPapers(
        searchQuery: String?, pageIndex: Int, pageSize: Int?,
        author: String?, journal: String?, publicationYearFrom: Int?,
        publicationYearTo: Int?
    ): PaginatedPapersListResponse
    fun getAllPapers(
        searchQuery: String?, pageIndex: Int, pageSize: Int?,
        author: String?, journal: String?, publicationYearFrom: Int?,
        publicationYearTo: Int?, userId: String?
    ): PaginatedPapersListResponse

    fun deleteById(id: String)
    fun createNewPaper(req: UploadPdfRequest, userId: String?)
    
    // Favorite methods
    fun addFavorite(paperId: String, userId: String)
    fun removeFavorite(paperId: String, userId: String)
    fun getFavoritePapersByUserId(userId: String): List<PaperResponse>
    fun isFavorite(paperId: String, userId: String): Boolean
}