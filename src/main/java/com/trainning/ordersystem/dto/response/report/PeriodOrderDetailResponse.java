package com.trainning.ordersystem.dto.response.report;

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
public class PeriodOrderDetailResponse {

    private Long id;

    private String orderCode;

    private Long customerId;

    private String customerName;

    private String customerPhone;

    private String customerEmail;

    private BigDecimal totalAmount;

    private OrderStatus status;

    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
}
