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
}
