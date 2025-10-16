package com.se1853_jv.service.impl

import com.se1853_jv.dto.response.TagResponse
import com.se1853_jv.repository.TagRepository
import com.se1853_jv.service.boundary.TagService
import org.springframework.stereotype.Service

@Service
class TagServiceImpl(
    private val repo: TagRepository
) : TagService {
    override fun getTagsByPaperId(id: String): List<TagResponse> {
        TODO("Not yet implemented")
    }
}