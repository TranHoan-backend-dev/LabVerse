package com.se1853_jv.service.boundary

import com.se1853_jv.dto.response.PaperResponse

interface PaperService {
    fun getPaperDetails(paperId: String): PaperResponse
}