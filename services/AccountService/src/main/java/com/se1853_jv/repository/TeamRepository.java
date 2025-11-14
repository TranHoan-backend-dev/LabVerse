package com.se1853_jv.repository;

import com.se1853_jv.model.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, String> {

    @Query("SELECT t FROM Team t WHERE t.privacy = 'PUBLIC' OR t.createdBy.id = :userId OR EXISTS " +
           "(SELECT tm FROM TeamMember tm WHERE tm.team.id = t.id AND tm.user.id = :userId)")
    Page<Team> findAccessibleTeams(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT t FROM Team t WHERE t.createdBy.id = :userId")
    List<Team> findByCreatedBy(@Param("userId") String userId);

    @Query("SELECT t FROM Team t JOIN TeamMember tm ON tm.team.id = t.id WHERE tm.user.id = :userId")
    List<Team> findByMemberId(@Param("userId") String userId);

    @Query("SELECT t FROM Team t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:researchField IS NULL OR t.researchField = :researchField) AND " +
           "(:privacy IS NULL OR t.privacy = :privacy) AND " +
           "(t.privacy = 'PUBLIC' OR t.createdBy.id = :userId OR EXISTS " +
           "(SELECT tm FROM TeamMember tm WHERE tm.team.id = t.id AND tm.user.id = :userId))")
    Page<Team> searchTeams(
            @Param("search") String search,
            @Param("researchField") String researchField,
            @Param("privacy") Team.PrivacyType privacy,
            @Param("userId") String userId,
            Pageable pageable);

    Optional<Team> findByIdAndCreatedById(String teamId, String userId);
    
    // Admin queries - get all teams without access restrictions
    @Query("SELECT t FROM Team t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:privacy IS NULL OR t.privacy = :privacy)")
    Page<Team> findAllForAdmin(@Param("search") String search,
                               @Param("privacy") Team.PrivacyType privacy,
                               Pageable pageable);
    
    // Statistics queries
    @Query("SELECT COUNT(t) FROM Team t WHERE t.privacy = 'PUBLIC'")
    Long countPublicTeams();
    
    @Query("SELECT COUNT(t) FROM Team t WHERE t.privacy = 'PRIVATE'")
    Long countPrivateTeams();
}

