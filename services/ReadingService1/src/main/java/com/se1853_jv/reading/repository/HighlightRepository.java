package com.se1853_jv.reading.repository;

import com.se1853_jv.reading.model.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HighlightRepository extends JpaRepository<Highlight, String> {
}

