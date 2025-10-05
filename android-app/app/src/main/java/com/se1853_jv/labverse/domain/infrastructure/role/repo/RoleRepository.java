package com.se1853_jv.labverse.domain.infrastructure.role.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.se1853_jv.labverse.domain.infrastructure.role.model.Roles;

@Dao
public interface RoleRepository {
    @Insert
    void create(Roles role);

    @Query("SELECT * FROM role WHERE id = :id")
    Roles getById(String id);
}
