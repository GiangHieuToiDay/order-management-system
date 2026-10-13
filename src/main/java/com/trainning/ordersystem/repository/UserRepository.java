package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import com.trainning.ordersystem.entity.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // lấy email của dùng cùng với role của người dùng luôn, left join lấy tất cả tài khoản dù có role hay k role
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    boolean existsByEmail(String email);


    @Query(
            value = """
            SELECT new com.trainning.ordersystem.dto.response.auth.UserProfileResponse(
                u.id, u.fullName, u.email, u.phone, r.name, u.status, u.createdAt, c.id, c.address, c.membershipLevel
            )
            FROM User u
            LEFT JOIN u.role r
            LEFT JOIN u.customer c
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR r.id = :roleId)
              AND (:status IS NULL OR u.status = :status)
        """,
            countQuery = """
            SELECT COUNT(u)
            FROM User u
            LEFT JOIN u.role r
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR r.id = :roleId)
              AND (:status IS NULL OR u.status = :status)
        """
    )
    Page<UserProfileResponse> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("status") UserStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.auth.UserProfileResponse(
            u.id, u.fullName, u.email, u.phone, r.name, u.status, u.createdAt, c.id, c.address, c.membershipLevel
        )
        FROM User u
        LEFT JOIN u.role r
        LEFT JOIN u.customer c
        WHERE u.id = :userId
    """)
    Optional<UserProfileResponse> findProfileById(@Param("userId") Long userId);

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.auth.UserProfileResponse(
            u.id, u.fullName, u.email, u.phone, r.name, u.status, u.createdAt, c.id, c.address, c.membershipLevel
        )
        FROM User u
        LEFT JOIN u.role r
        JOIN u.customer c
        WHERE c.id = :customerId
    """)
    Optional<UserProfileResponse> findProfileByCustomerId(@Param("customerId") Long customerId);

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.auth.UserProfileResponse(
            u.id, u.fullName, u.email, u.phone, r.name, u.status, u.createdAt, c.id, c.address, c.membershipLevel
        )
        FROM User u
        LEFT JOIN u.role r
        LEFT JOIN u.customer c
        WHERE u.email = :email
    """)
    Optional<UserProfileResponse> findProfileByEmail(@Param("email") String email);
}
