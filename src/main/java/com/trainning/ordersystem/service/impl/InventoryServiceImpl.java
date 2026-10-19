package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.repository.InventoryTransactionRepository;
import com.trainning.ordersystem.repository.InventoryTransactionRepositoryCustom;
import com.trainning.ordersystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final InventoryTransactionRepositoryCustom inventoryTransactionRepositoryCustom;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryTransactionResponse> getAllTransactions(int page, int size) {
        log.info("Lấy danh sách tất cả giao dịch kho: page={}, size={}", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryTransactionResponse> transactionPage = inventoryTransactionRepositoryCustom.findAllTransactionResponses(pageable);
        return buildPageResponse(transactionPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<InventoryTransactionResponse> getTransactionsByProduct(Long productId, int page, int size) {
        log.info("Lấy lịch sử giao dịch kho theo sản phẩm: productId={}, page={}, size={}", productId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryTransactionResponse> transactionPage = inventoryTransactionRepositoryCustom.findTransactionResponsesByProductId(productId, pageable);
        return buildPageResponse(transactionPage);
    }

    private PageResponse<InventoryTransactionResponse> buildPageResponse(Page<InventoryTransactionResponse> page) {
        return PageResponse.<InventoryTransactionResponse>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .build();
    }
}
