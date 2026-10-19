package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // lấy email của dùng cùng với role của người dùng luôn, left join lấy tất cả tài khoản dù có role hay k role
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    boolean existsByEmail(String email);

    @NativeQuery(
        value = """
            SELECT u.id AS id,
                   u.full_name AS fullName,
                   u.email AS email,
                   u.phone AS phone,
                   r.name AS role,
                   u.status AS status,
                   u.created_at AS createdAt,
                   c.id AS customerId,
                   c.address AS address,
                   c.membership_level AS membershipLevel
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            LEFT JOIN customers c ON c.user_id = u.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR r.id = :roleId)
              AND (:status IS NULL OR u.status = :status)
        """,
        countQuery = """
            SELECT COUNT(u.id)
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR r.id = :roleId)
              AND (:status IS NULL OR u.status = :status)
        """,
        sqlResultSetMapping = "UserProfileResponseMapping"
    )
    Page<UserProfileResponse> searchUsers(
            @Param("keyword") String keyword,
            @Param("roleId") Long roleId,
            @Param("status") String status,
            Pageable pageable
    );

    @NativeQuery(
        value = """
            SELECT u.id AS id,
                   u.full_name AS fullName,
                   u.email AS email,
                   u.phone AS phone,
                   r.name AS role,
                   u.status AS status,
                   u.created_at AS createdAt,
                   c.id AS customerId,
                   c.address AS address,
                   c.membership_level AS membershipLevel
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            LEFT JOIN customers c ON c.user_id = u.id
            WHERE u.id = :userId
        """,
        sqlResultSetMapping = "UserProfileResponseMapping"
    )
    Optional<UserProfileResponse> findProfileById(@Param("userId") Long userId);

    @NativeQuery(
        value = """
            SELECT u.id AS id,
                   u.full_name AS fullName,
                   u.email AS email,
                   u.phone AS phone,
                   r.name AS role,
                   u.status AS status,
                   u.created_at AS createdAt,
                   c.id AS customerId,
                   c.address AS address,
                   c.membership_level AS membershipLevel
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            JOIN customers c ON c.user_id = u.id
            WHERE c.id = :customerId
        """,
        sqlResultSetMapping = "UserProfileResponseMapping"
    )
    Optional<UserProfileResponse> findProfileByCustomerId(@Param("customerId") Long customerId);

    @NativeQuery(
        value = """
            SELECT u.id AS id,
                   u.full_name AS fullName,
                   u.email AS email,
                   u.phone AS phone,
                   r.name AS role,
                   u.status AS status,
                   u.created_at AS createdAt,
                   c.id AS customerId,
                   c.address AS address,
                   c.membership_level AS membershipLevel
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            LEFT JOIN customers c ON c.user_id = u.id
            WHERE u.email = :email
        """,
        sqlResultSetMapping = "UserProfileResponseMapping"
    )
    Optional<UserProfileResponse> findProfileByEmail(@Param("email") String email);
}
