package com.innowise.user_service.repository;

import com.innowise.user_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor {
    @Modifying
    @Query(value = "UPDATE users SET active = :active WHERE id = :id", nativeQuery = true)
    void updateActiveStatusNative(@Param("id") Long id, @Param("active") boolean active);
}