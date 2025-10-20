package com.se1853_jv.repository;


import com.se1853_jv.model.CollectionPaper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionPaperRepository extends JpaRepository<CollectionPaper, String> {
    List<CollectionPaper> findByCollectionId(String collectionId);
}
