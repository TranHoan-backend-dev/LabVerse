package com.se1853_jv.labverse.domain.infrastructure.team.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.team.model.Team;

@Dao
public interface TeamRepository {
    @Insert
    void create(Team team);

    @Update
    void update(Team team);

    @Delete
    void deleteById(Team team);

    @Query("SELECT * FROM Team WHERE id = :id")
    Team getById(String id);
}
