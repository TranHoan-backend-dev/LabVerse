package com.se1853_jv.service.impl

import com.se1853_jv.repository.HighlightRepository
import com.se1853_jv.service.boundary.HighlightService
import org.springframework.stereotype.Service

@Service
class HighlightServiceImpl(
    private val repo: HighlightRepository
) : HighlightService {
}