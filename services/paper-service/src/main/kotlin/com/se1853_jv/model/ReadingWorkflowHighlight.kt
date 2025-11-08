package com.se1853_jv.model

import jakarta.persistence.*
import java.io.Serializable

/**
 * Junction table: ReadingWorkflow_Highlight
 * Link Highlight với ReadingWorkflow (many-to-many relationship)
 */
@Entity
@Table(name = "ReadingWorkflow_Highlight")
@IdClass(ReadingWorkflowHighlightId::class)
data class ReadingWorkflowHighlight(
    @Id
    @Column(name = "RWCollectionId", length = 36, nullable = false)
    val rwCollectionId: String,

    @Id
    @Column(name = "RWPaperId", length = 36, nullable = false)
    val rwPaperId: String,

    @Id
    @Column(name = "RWUsersid", length = 36, nullable = false)
    val rwUserId: String,

    @Id
    @Column(name = "Highlightid", length = 36, nullable = false)
    val highlightId: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "RWCollectionId", referencedColumnName = "CollectionId", insertable = false, updatable = false),
        JoinColumn(name = "RWPaperId", referencedColumnName = "PaperId", insertable = false, updatable = false),
        JoinColumn(name = "RWUsersid", referencedColumnName = "UsersId", insertable = false, updatable = false)
    )
    val readingWorkflow: ReadingWorkflow? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Highlightid", referencedColumnName = "id", insertable = false, updatable = false)
    val highlight: Highlight? = null
)

data class ReadingWorkflowHighlightId(
    val rwCollectionId: String,
    val rwPaperId: String,
    val rwUserId: String,
    val highlightId: String
) : Serializable







