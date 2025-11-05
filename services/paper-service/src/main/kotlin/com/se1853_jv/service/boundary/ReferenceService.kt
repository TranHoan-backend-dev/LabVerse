package com.se1853_jv.service.boundary

import com.se1853_jv.dto.response.ReferenceResponse

interface ReferenceService {
    fun getReferencesByPaperId(id: String): List<ReferenceResponse>
    fun getReferenceById(id: String): ReferenceResponse
}