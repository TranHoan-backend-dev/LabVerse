package com.se1853_jv.service.boundary

import com.se1853_jv.dto.response.TagResponse

interface TagService {
    fun getTagsByPaperId(id: String): List<TagResponse>
    fun getTheFiveMostPopularTag(): List<TagResponse>
}