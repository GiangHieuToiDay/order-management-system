package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventory-transactions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<InventoryTransactionResponse> response = inventoryService.getAllTransactions(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách lịch sử giao dịch kho thành công", response));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<PageResponse<InventoryTransactionResponse>>> getTransactionsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<InventoryTransactionResponse> response = inventoryService.getTransactionsByProduct(productId, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử giao dịch kho của sản phẩm thành công", response));
    }
}
