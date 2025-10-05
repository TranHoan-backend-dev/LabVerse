package com.se1853_jv.labverse.domain.infrastructure.collection.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.collection.model.Collections;

@Dao
public interface CollectionRepository {
    @Insert
    void create(Collections collection);

    @Update
    void update(Collections collection);

    @Delete
    void delete(Collections collection);

    @Query("DELETE FROM Collections WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Collections WHERE id = :id")
    Collections getById(String id);
}
