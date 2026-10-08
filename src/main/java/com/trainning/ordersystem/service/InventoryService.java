package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;

public interface InventoryService {

    /**
     * Lấy toàn bộ lịch sử biến động kho (phân trang, mới nhất lên trước).
     * Actor: ADMIN
     */
    PageResponse<InventoryTransactionResponse> getAllTransactions(int page, int size);

    /**
     * Lấy lịch sử biến động kho theo sản phẩm (phân trang, mới nhất lên trước).
     * Actor: ADMIN
     */
    PageResponse<InventoryTransactionResponse> getTransactionsByProduct(Long productId, int page, int size);
}
