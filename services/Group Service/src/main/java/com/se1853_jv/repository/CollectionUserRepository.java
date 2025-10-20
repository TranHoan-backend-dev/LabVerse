package com.se1853_jv.repository;


import com.se1853_jv.model.CollectionUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionUserRepository extends JpaRepository<CollectionUser, String> {
    List<CollectionUser> findByCollectionId(String collectionId);
}
