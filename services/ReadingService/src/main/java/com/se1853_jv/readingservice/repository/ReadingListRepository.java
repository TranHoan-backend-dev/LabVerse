package com.se1853_jv.readingservice.repository;

import com.se1853_jv.readingservice.model.ReadingList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReadingListRepository extends JpaRepository<ReadingList, UUID> {
    
    @Query("SELECT rl FROM ReadingList rl " +
           "WHERE rl.userIdsList LIKE CONCAT('%', :userId, '%')")
    List<ReadingList> findByUserId(@Param("userId") String userId);
}

