package com.se1853_jv.repository;

import com.se1853_jv.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByGoogleId(String googleId);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    // Admin queries
    @Query("SELECT u FROM User u " +
           "WHERE (:search IS NULL OR :search = '' OR " +
           "      LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "      LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "      LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:role IS NULL OR :role = '' OR u.role.name = :role) " +
           "AND (:isActive IS NULL OR u.isActive = :isActive)")
    Page<User> findAllWithFilters(@Param("search") String search,
                                  @Param("role") String role,
                                  @Param("isActive") Boolean isActive,
                                  Pageable pageable);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    Long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = false OR u.isActive IS NULL")
    Long countInactiveUsers();
}













