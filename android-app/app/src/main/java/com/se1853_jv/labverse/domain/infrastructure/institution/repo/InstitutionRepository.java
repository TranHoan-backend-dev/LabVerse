package com.se1853_jv.labverse.domain.infrastructure.institution.repo;

import androidx.room.*;

import com.se1853_jv.labverse.domain.infrastructure.institution.model.Institution;

@Dao
public interface InstitutionRepository {
    @Insert
    void create(Institution institution);

    @Update
    void update(Institution institution);

    @Delete
    void delete(Institution institution);

    @Query("DELETE FROM Institution WHERE id = :id")
    void deleteById(String id);

    @Query("SELECT * FROM Institution WHERE id = :id")
    Institution getById(String id);
}
