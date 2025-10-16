package com.se1853_jv.service.boundary

import com.se1853_jv.dto.response.CitationResponse

interface CitationService {
    fun getCitationsByPaperId(id: String): List<CitationResponse>
    fun getCitationById(id: String): CitationResponse
}