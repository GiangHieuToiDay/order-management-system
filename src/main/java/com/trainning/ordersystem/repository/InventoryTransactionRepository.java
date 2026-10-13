package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse(
            t.id, p.id, p.name, p.sku, t.type, t.quantity, t.reason, u.id, u.fullName, t.createdAt
        )
        FROM InventoryTransaction t
        LEFT JOIN t.product p
        LEFT JOIN t.createdBy u
        ORDER BY t.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(t) FROM InventoryTransaction t
    """)
    Page<InventoryTransactionResponse> findAllTransactionResponses(Pageable pageable);

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse(
            t.id, p.id, p.name, p.sku, t.type, t.quantity, t.reason, u.id, u.fullName, t.createdAt
        )
        FROM InventoryTransaction t
        LEFT JOIN t.product p
        LEFT JOIN t.createdBy u
        WHERE t.product.id = :productId
        ORDER BY t.createdAt DESC
    """,
    countQuery = """
        SELECT COUNT(t) FROM InventoryTransaction t WHERE t.product.id = :productId
    """)
    Page<InventoryTransactionResponse> findTransactionResponsesByProductId(@Param("productId") Long productId, Pageable pageable);
}
