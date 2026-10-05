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
public class SalesSummaryResponse {

    private BigDecimal todayRevenue;
    private long totalOrdersToday;
    private long pendingOrders;
    private long lowStockProductsCount;
}
