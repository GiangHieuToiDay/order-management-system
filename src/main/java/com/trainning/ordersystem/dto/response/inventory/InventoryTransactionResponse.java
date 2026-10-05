package com.trainning.ordersystem.dto.response.inventory;

import com.trainning.ordersystem.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private TransactionType type;
    private Integer quantity;
    private Integer balanceAfter;
    private String referenceCode;
    private String note;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
