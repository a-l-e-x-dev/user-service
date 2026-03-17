package com.innowise.user_service.repository;

import com.innowise.user_service.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor {
    @Modifying
    @Query(value = "UPDATE users SET active = :active WHERE id = :id", nativeQuery = true)
    void updateActiveStatusNative(@Param("id") Long id, @Param("active") boolean active);

    @EntityGraph(attributePaths = {"paymentCards"})
    Optional<User> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}