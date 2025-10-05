package com.se1853_jv.labverse.domain.infrastructure.readinglist.repo;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.readinglist.model.ReadingList;

@Dao
public interface ReadingListRepository {
    @Insert
    void create(ReadingList reading);

    @Update
    void update(ReadingList reading);

    @Delete
    void delete(ReadingList reading);

    @Query("DELETE FROM ReadingList WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM ReadingList WHERE id = :id")
    ReadingList getById(String id);
}
