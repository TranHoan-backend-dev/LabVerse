package com.se1853_jv.labverse.domain.infrastructure.team.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;

import java.util.List;

@Dao
public interface TeamRepository {
    @Insert
    void create(Team team);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Team> teams);

    @Update
    void update(Team team);

    @Delete
    void deleteById(Team team);

    @Query("DELETE FROM Team")
    void deleteAll();

    @Query("SELECT * FROM Team WHERE id = :id")
    Team getById(String id);

    @Query("SELECT * FROM Team ORDER BY created_date DESC")
    List<Team> getAll();

    @Query("SELECT * FROM Team WHERE name LIKE '%' || :search || '%' OR description LIKE '%' || :search || '%'")
    List<Team> searchTeams(String search);

    @Query("SELECT * FROM Team WHERE research_field = :researchField")
    List<Team> getByResearchField(String researchField);

    @Query("SELECT * FROM Team WHERE privacy = :privacy")
    List<Team> getByPrivacy(String privacy);
}
