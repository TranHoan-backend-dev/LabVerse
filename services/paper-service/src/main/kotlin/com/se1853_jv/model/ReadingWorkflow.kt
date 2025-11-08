package com.se1853_jv.model

import jakarta.persistence.*
import java.io.Serializable

/**
 * ReadingWorkflow entity - Track reading progress của user cho paper trong collection
 * Composite primary key (CollectionId, PaperId, UsersId)
 */
@Entity
@Table(name = "ReadingWorkflow")
@IdClass(ReadingWorkflowId::class)
data class ReadingWorkflow(
    @Id
    @Column(name = "CollectionId", length = 36, nullable = false)
    val collectionId: String,

    @Id
    @Column(name = "PaperId", length = 36, nullable = false)
    val paperId: String,

    @Id
    @Column(name = "UsersId", length = 36, nullable = false)
    val userId: String,

    @Column(name = "status", length = 10)
    val status: String? = null,

    @Column(name = "last_page")
    val lastPage: Int? = null,

    @Column(name = "progress")
    val progress: Int? = null
)

/**
 * Composite key class for ReadingWorkflow
 */
data class ReadingWorkflowId(
    val collectionId: String,
    val paperId: String,
    val userId: String
) : Serializable







