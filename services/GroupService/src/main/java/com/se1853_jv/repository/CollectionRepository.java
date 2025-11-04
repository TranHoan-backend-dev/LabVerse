package com.se1853_jv.repository;

import com.se1853_jv.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, String> {
    boolean existsByName(String name);

}
