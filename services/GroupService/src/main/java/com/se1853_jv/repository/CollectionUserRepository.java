package com.se1853_jv.repository;

import com.se1853_jv.model.CollectionUser;
import com.se1853_jv.model.CollectionUserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionUserRepository extends JpaRepository<CollectionUser, CollectionUserId> {
    List<CollectionUser> findByIdCollectionId(String collectionId);

    List<CollectionUser> findByIdMemberId(String memberId);

    List<CollectionUser> findByIdMemberIdAndIsAuthor(String memberId, Boolean isAuthor);
}
