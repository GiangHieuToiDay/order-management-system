package com.trainning.ordersystem.dto.response.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subTotal;

    public CartItemResponse(
            Long id,
            Long productId,
            String productName,
            String productSku,
            String productImage,
            BigDecimal unitPrice,
            Integer quantity
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productImage = productImage;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subTotal = (unitPrice != null && quantity != null)
                ? unitPrice.multiply(BigDecimal.valueOf(quantity))
                : BigDecimal.ZERO;
    }
}
