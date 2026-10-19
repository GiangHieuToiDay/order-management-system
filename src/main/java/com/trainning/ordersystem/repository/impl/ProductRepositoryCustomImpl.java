package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.repository.ProductRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Optional<ProductDetailResponse> findDetailById(Long id) {
        String sql = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.stock_quantity AS stockQuantity,
                   p.description AS description,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status,
                   p.created_at AS createdAt,
                   p.updated_at AS updatedAt
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = :id
        """;

        Query query = entityManager.createNativeQuery(sql, "ProductDetailMapping");
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<ProductDetailResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<ProductDetailResponse> findDetailBySku(String sku) {
        String sql = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.stock_quantity AS stockQuantity,
                   p.description AS description,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status,
                   p.created_at AS createdAt,
                   p.updated_at AS updatedAt
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.sku = :sku
        """;

        Query query = entityManager.createNativeQuery(sql, "ProductDetailMapping");
        query.setParameter("sku", sku);

        @SuppressWarnings("unchecked")
        List<ProductDetailResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<ProductSummaryResponse> findSummaryById(Long id) {
        String sql = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE p.id = :id
        """;

        Query query = entityManager.createNativeQuery(sql, "ProductSummaryMapping");
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<ProductSummaryResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Page<ProductSummaryResponse> searchProductSummaries(
            String keyword,
            Long categoryId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        String sql = """
            SELECT p.id AS id,
                   p.name AS name,
                   p.sku AS sku,
                   p.price AS price,
                   p.unit AS unit,
                   p.image_url AS imageUrl,
                   c.id AS categoryId,
                   c.name AS categoryName,
                   p.status AS status
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:status IS NULL OR :status = '' OR p.status = :status)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            ORDER BY p.id DESC
        """;

        String countSql = """
            SELECT COUNT(p.id)
            FROM products p
            LEFT JOIN categories c ON p.category_id = c.id
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:categoryId IS NULL OR c.id = :categoryId)
              AND (:status IS NULL OR :status = '' OR p.status = :status)
              AND (:minPrice IS NULL OR p.price >= :minPrice)
              AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        """;

        Query query = entityManager.createNativeQuery(sql, "ProductSummaryMapping");
        query.setParameter("keyword", keyword);
        query.setParameter("categoryId", categoryId);
        query.setParameter("status", status);
        query.setParameter("minPrice", minPrice);
        query.setParameter("maxPrice", maxPrice);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<ProductSummaryResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("keyword", keyword);
        countQuery.setParameter("categoryId", categoryId);
        countQuery.setParameter("status", status);
        countQuery.setParameter("minPrice", minPrice);
        countQuery.setParameter("maxPrice", maxPrice);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
}