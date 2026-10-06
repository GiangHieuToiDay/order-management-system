package com.trainning.ordersystem.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRevenueStatisticResponse {

    private String period;

    private Long totalOrders;

    private Long completedOrders;

    private Long confirmedOrders;

    private Long cancelledOrders;

    private Long pendingOrders;

    private BigDecimal totalRevenue;

    private Double successRate;

    private Double cancellationRate;
}
