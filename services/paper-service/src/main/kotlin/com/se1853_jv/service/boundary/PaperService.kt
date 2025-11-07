package com.se1853_jv.service.boundary

import com.se1853_jv.dto.request.SearchPapersRequest
import com.se1853_jv.dto.request.UploadPdfRequest
import com.se1853_jv.dto.response.PaperResponse

interface PaperService {
    fun getPaperDetails(paperId: String): PaperResponse
    fun getAllPapers(searchQuery: String?, pageIndex: Int, pageSize: Int?): List<PaperResponse>
    fun searchPapersWithFilters(request: SearchPapersRequest): List<PaperResponse>
    fun deleteById(id: String)
    fun createNewPaper(req: UploadPdfRequest)
}