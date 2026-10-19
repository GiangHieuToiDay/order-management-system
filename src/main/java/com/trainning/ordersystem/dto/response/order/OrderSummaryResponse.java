package com.trainning.ordersystem.dto.response.order;

import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private Long id;
    private String orderCode;
    private Long customerId;
    private String customerName;
    private String recipientName;
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;
    private int totalItems;
    private LocalDateTime createdAt;

    // Constructor dùng cho SqlResultSetMapping
    public OrderSummaryResponse(
            Long id,
            String orderCode,
            Long customerId,
            String customerName,
            String recipientName,
            BigDecimal totalAmount,
            String status,
            String paymentMethod,
            Integer totalItems,
            LocalDateTime createdAt
    ) {
        this(
                id,
                orderCode,
                customerId,
                customerName,
                recipientName,
                totalAmount,
                status != null ? OrderStatus.valueOf(status) : null,
                paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : null,
                totalItems != null ? totalItems : 0,
                createdAt
        );
    }
}
