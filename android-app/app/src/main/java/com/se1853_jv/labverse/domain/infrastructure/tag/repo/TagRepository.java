package com.se1853_jv.labverse.domain.infrastructure.tag.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.tag.model.Tag;

@Dao
public interface TagRepository {
    @Insert
    void create(Tag tag);

    @Update
    void update(Tag tag);

    @Delete
    void delete(Tag tag);

    @Query("DELETE FROM Tag WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Tag WHERE id = :id")
    Tag getById(String id);
}
