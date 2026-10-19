package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InventoryTransactionRepositoryCustom {

    Page<InventoryTransactionResponse> findAllTransactionResponses(Pageable pageable);

    Page<InventoryTransactionResponse> findTransactionResponsesByProductId(Long productId, Pageable pageable);
}
