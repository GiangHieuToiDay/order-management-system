package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    @NativeQuery(
        value = """
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
        """,
        countQuery = """
            SELECT COUNT(t.id) FROM inventory_transactions t
        """,
        sqlResultSetMapping = "InventoryTransactionResponseMapping"
    )
    Page<InventoryTransactionResponse> findAllTransactionResponses(Pageable pageable);

    @NativeQuery(
        value = """
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
        """,
        countQuery = """
            SELECT COUNT(t.id) FROM inventory_transactions t WHERE t.product_id = :productId
        """,
        sqlResultSetMapping = "InventoryTransactionResponseMapping"
    )
    Page<InventoryTransactionResponse> findTransactionResponsesByProductId(@Param("productId") Long productId, Pageable pageable);
}
