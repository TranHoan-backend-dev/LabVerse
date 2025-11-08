package com.se1853_jv.labverse.domain.infrastructure.sync.repo;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.se1853_jv.labverse.domain.infrastructure.sync.model.SyncQueue;

import java.util.List;

@Dao
public interface SyncQueueRepository {
    @Query("SELECT * FROM SyncQueue WHERE isSynced = 0 ORDER BY createdAt ASC LIMIT :limit")
    List<SyncQueue> getPendingSyncs(int limit);

    @Query("SELECT * FROM SyncQueue WHERE isSynced = 0")
    List<SyncQueue> getAllPendingSyncs();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SyncQueue syncQueue);

    @Update
    void update(SyncQueue syncQueue);

    @Query("DELETE FROM SyncQueue WHERE isSynced = 1")
    void deleteSyncedItems();

    @Query("SELECT COUNT(*) FROM SyncQueue WHERE isSynced = 0")
    int getPendingSyncCount();

    @Query("DELETE FROM SyncQueue WHERE id = :id")
    void deleteById(Long id);
}











