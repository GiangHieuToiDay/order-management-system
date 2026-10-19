package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.auth.UserProfileResponse;
import com.trainning.ordersystem.repository.UserRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<UserProfileResponse> searchUsers(
            String keyword,
            Long roleId,
            String status,
            Pageable pageable
    ) {
        String sql = """
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
              AND (:status IS NULL OR :status = '' OR u.status = :status)
            ORDER BY u.id DESC
        """;

        String countSql = """
            SELECT COUNT(u.id)
            FROM users u
            LEFT JOIN roles r ON u.role_id = r.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:roleId IS NULL OR r.id = :roleId)
              AND (:status IS NULL OR :status = '' OR u.status = :status)
        """;

        Query query = entityManager.createNativeQuery(sql, "UserProfileResponseMapping");
        query.setParameter("keyword", keyword);
        query.setParameter("roleId", roleId);
        query.setParameter("status", status);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<UserProfileResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("keyword", keyword);
        countQuery.setParameter("roleId", roleId);
        countQuery.setParameter("status", status);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Optional<UserProfileResponse> findProfileById(Long userId) {
        String sql = """
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
        """;

        Query query = entityManager.createNativeQuery(sql, "UserProfileResponseMapping");
        query.setParameter("userId", userId);

        @SuppressWarnings("unchecked")
        List<UserProfileResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<UserProfileResponse> findProfileByCustomerId(Long customerId) {
        String sql = """
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
        """;

        Query query = entityManager.createNativeQuery(sql, "UserProfileResponseMapping");
        query.setParameter("customerId", customerId);

        @SuppressWarnings("unchecked")
        List<UserProfileResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}