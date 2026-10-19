package com.Group5.ISMS.repository;

import com.Group5.ISMS.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameAndIsDeletedFalse(String username);

    Optional<User> findByEmailAndIsDeletedFalse(String email);

    Optional<User> findByUserIdAndIsDeletedFalse(Long userId);

    Optional<User> findByUserIdAndIsDeletedTrue(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsernameAndIsDeletedFalse(String username);

    boolean existsByEmailAndIsDeletedFalse(String email);

    List<User> findByIsDeletedFalseOrderByUserIdAsc();

    List<User> findByIsDeletedTrueOrderByUserIdAsc();

    long countByIsDeletedFalse();

    long countByIsDeletedTrue();

    @Query("SELECT u FROM User u WHERE u.isDeleted = false AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')))" +
           " ORDER BY u.userId ASC")
    List<User> searchActiveUsers(@Param("kw") String keyword);

    @Query("SELECT u FROM User u WHERE u.isDeleted = true AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :kw, '%')))" +
           " ORDER BY u.userId ASC")
    List<User> searchLockedUsers(@Param("kw") String keyword);

    long countByRoleAndIsDeletedFalse(String role);
}
