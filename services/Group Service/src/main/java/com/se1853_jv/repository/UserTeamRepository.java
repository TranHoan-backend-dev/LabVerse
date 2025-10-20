package com.se1853_jv.repository;

import com.se1853_jv.model.UserTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserTeamRepository extends JpaRepository<UserTeam, String> {
    List<UserTeam> findByTeamId(String teamId);
}