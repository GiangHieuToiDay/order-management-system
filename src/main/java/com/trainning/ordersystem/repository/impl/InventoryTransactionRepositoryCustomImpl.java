package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.repository.InventoryTransactionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InventoryTransactionRepositoryCustomImpl implements InventoryTransactionRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<InventoryTransactionResponse> findAllTransactionResponses(Pageable pageable) {
        String sql = """
            SELECT t.id AS id,
                   p.id AS productId,
                   p.name AS productName,
                   p.sku AS productSku,
                   t.type AS type,
                   t.quantity AS quantity,
                   t.reason AS note,
                   u.id AS createdBy,
                   u.full_name AS createdByName,
                   t.created_at AS createdAt
            FROM inventory_transactions t
            LEFT JOIN products p ON t.product_id = p.id
            LEFT JOIN users u ON t.created_by = u.id
            ORDER BY t.created_at DESC
        """;

        String countSql = "SELECT COUNT(t.id) FROM inventory_transactions t";

        Query query = entityManager.createNativeQuery(sql, "InventoryTransactionResponseMapping");
        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<InventoryTransactionResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<InventoryTransactionResponse> findTransactionResponsesByProductId(Long productId, Pageable pageable) {
        String sql = """
            SELECT t.id AS id,
                   p.id AS productId,
                   p.name AS productName,
                   p.sku AS productSku,
                   t.type AS type,
                   t.quantity AS quantity,
                   t.reason AS note,
                   u.id AS createdBy,
                   u.full_name AS createdByName,
                   t.created_at AS createdAt
            FROM inventory_transactions t
            LEFT JOIN products p ON t.product_id = p.id
            LEFT JOIN users u ON t.created_by = u.id
            WHERE t.product_id = :productId
            ORDER BY t.created_at DESC
        """;

        String countSql = "SELECT COUNT(t.id) FROM inventory_transactions t WHERE t.product_id = :productId";

        Query query = entityManager.createNativeQuery(sql, "InventoryTransactionResponseMapping");
        query.setParameter("productId", productId);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<InventoryTransactionResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("productId", productId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }
}