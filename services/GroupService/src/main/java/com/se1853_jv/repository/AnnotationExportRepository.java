package com.se1853_jv.repository;

import com.se1853_jv.model.AnnotationExport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnotationExportRepository extends JpaRepository<AnnotationExport, String> {

    List<AnnotationExport> findByCollectionIdOrderByExportedAtDesc(String collectionId);

    List<AnnotationExport> findByPaperIdAndCollectionIdOrderByExportedAtDesc(String paperId, String collectionId);
}

