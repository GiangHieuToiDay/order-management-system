package com.trainning.ordersystem.dto.response.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Builder.Default
    private List<PeriodOrderDetailResponse> orders = new ArrayList<>();
}

