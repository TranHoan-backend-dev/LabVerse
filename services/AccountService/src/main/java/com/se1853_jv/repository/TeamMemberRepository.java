package com.se1853_jv.repository;

import com.se1853_jv.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, String> {

    Optional<TeamMember> findByTeamIdAndUserId(String teamId, String userId);

    List<TeamMember> findByTeamId(String teamId);

    List<TeamMember> findByUserId(String userId);

    @Query("SELECT tm FROM TeamMember tm WHERE tm.team.id = :teamId AND tm.user.id = :userId")
    Optional<TeamMember> findMemberInTeam(@Param("teamId") String teamId, @Param("userId") String userId);

    boolean existsByTeamIdAndUserId(String teamId, String userId);

    void deleteByTeamIdAndUserId(String teamId, String userId);
}

