package com.se1853_jv.service.impl

import com.se1853_jv.repository.NoteRepository
import com.se1853_jv.service.boundary.NoteService
import org.springframework.stereotype.Service

@Service
class NoteServiceImpl(
    private val repo: NoteRepository
) : NoteService {
}