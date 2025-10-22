package com.se1853_jv.repository;

import com.se1853_jv.model.CollectionUser;
import com.se1853_jv.model.CollectionUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CollectionUserRepository extends JpaRepository<CollectionUser, CollectionUserId> {
    List<CollectionUser> findByIdCollectionId(String collectionId);
}
