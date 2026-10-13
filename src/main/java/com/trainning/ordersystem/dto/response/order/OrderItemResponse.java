package com.trainning.ordersystem.dto.response.order;

import com.trainning.ordersystem.entity.OrderItem;
import com.trainning.ordersystem.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
public class OrderItemResponse {


    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private String productImage;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subTotal;


    public OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String productSku,
            String productImage,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subTotal
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productImage = productImage;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subTotal = subTotal;
    }

    public OrderItemResponse(
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

    public OrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String productSku,
            String productImage,
            BigDecimal unitPrice,
            Integer quantity,
            Number subTotal
    ) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.productImage = productImage;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.subTotal = (subTotal != null) ? BigDecimal.valueOf(subTotal.doubleValue()) : BigDecimal.ZERO;
    }

    public static OrderItemResponse from(OrderItem item) {
        if (item == null) return null;
        Product p = item.getProduct();

        BigDecimal subTotal = BigDecimal.ZERO;

        if (item.getUnitPrice() != null && item.getQuantity() != null) {
            subTotal = item.getUnitPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
        }

        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(p != null ? p.getId() : null)
                .productName(p != null ? p.getName() : null)
                .productSku(p != null ? p.getSku() : null)
                .productImage(p != null ? p.getImageUrl() : null)
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subTotal(subTotal)
                .build();
    }
}
