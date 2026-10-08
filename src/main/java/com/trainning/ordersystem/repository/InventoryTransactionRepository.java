package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByProductId(Long productId, Pageable pageable);
    Page<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    Page<InventoryTransaction> findAllByOrderByCreatedAtDesc(Pageable pageable);
    List<InventoryTransaction> findByProductIdOrderByCreatedAtDesc(Long productId);
}
