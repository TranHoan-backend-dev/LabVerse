package com.se1853_jv.labverse.domain.infrastructure.user.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.user.model.Users;

@Dao
public interface UserRepository {
    @Insert
    void create(Users user);

    @Update
    void update(Users user);

    @Delete
    void deleteById(Users user);

    @Query("SELECT * FROM Users WHERE id = :id")
    Users getById(String id);
}
