package com.se1853_jv.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.se1853_jv.model.CollectionPaper;
import com.se1853_jv.model.CollectionPaperId;

public interface CollectionPaperRepository extends JpaRepository<CollectionPaper, CollectionPaperId> { }


