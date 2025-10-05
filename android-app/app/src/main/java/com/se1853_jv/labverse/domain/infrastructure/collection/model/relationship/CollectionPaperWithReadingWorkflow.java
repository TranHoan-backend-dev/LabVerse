package com.se1853_jv.labverse.domain.infrastructure.collection.model.relationship;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.se1853_jv.labverse.domain.infrastructure.ref.CollectionPaperCrossRef;
import com.se1853_jv.labverse.domain.infrastructure.workflow.model.ReadingWorkflow;

import java.util.List;

public class CollectionPaperWithReadingWorkflow {
    @Embedded
    public CollectionPaperCrossRef collectionPaper;

    @Relation(
            parentColumn = "paperId",
            entityColumn = "paperId",
            entity = ReadingWorkflow.class
    )
    public List<ReadingWorkflow> readingWorkflows;
}
