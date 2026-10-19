package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.category.CategoryResponse;
import com.trainning.ordersystem.repository.CategoryRepositoryCustom;
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
public class CategoryRepositoryCustomImpl implements CategoryRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<CategoryResponse> searchCategoryResponses(String keyword, String status, Pageable pageable) {
        String sql = """
            SELECT c.id AS id,
                   c.name AS name,
                   c.description AS description,
                   c.status AS status,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt
            FROM categories c
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR :status = '' OR c.status = :status)
            ORDER BY c.id DESC
        """;

        String countSql = """
            SELECT COUNT(c.id)
            FROM categories c
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR :status = '' OR c.status = :status)
        """;

        Query query = entityManager.createNativeQuery(sql, "CategoryResponseMapping");
        query.setParameter("keyword", keyword);
        query.setParameter("status", status);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<CategoryResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("keyword", keyword);
        countQuery.setParameter("status", status);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<CategoryResponse> findCategoryResponsesByStatus(String status) {
        String sql = """
            SELECT c.id AS id,
                   c.name AS name,
                   c.description AS description,
                   c.status AS status,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt
            FROM categories c
            WHERE c.status = :status
            ORDER BY c.id DESC
        """;

        Query query = entityManager.createNativeQuery(sql, "CategoryResponseMapping");
        query.setParameter("status", status);

        @SuppressWarnings("unchecked")
        List<CategoryResponse> results = query.getResultList();
        return results;
    }

    @Override
    public Optional<CategoryResponse> findCategoryResponseById(Long id) {
        String sql = """
            SELECT c.id AS id,
                   c.name AS name,
                   c.description AS description,
                   c.status AS status,
                   c.created_at AS createdAt,
                   c.updated_at AS updatedAt
            FROM categories c
            WHERE c.id = :id
        """;

        Query query = entityManager.createNativeQuery(sql, "CategoryResponseMapping");
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<CategoryResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}