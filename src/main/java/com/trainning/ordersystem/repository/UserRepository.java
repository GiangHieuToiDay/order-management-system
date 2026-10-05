package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
            "(:keyword IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            " OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:roleId IS NULL OR u.role.id = :roleId) " +
            "AND (:status IS NULL OR u.status = :status)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("roleId") Long roleId,
                           @Param("status") UserStatus status,
                           Pageable pageable);
}
