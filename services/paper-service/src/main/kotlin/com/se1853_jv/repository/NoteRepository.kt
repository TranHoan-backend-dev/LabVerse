package com.se1853_jv.repository

import com.se1853_jv.model.Note
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface NoteRepository : MongoRepository<Note, String> {
}