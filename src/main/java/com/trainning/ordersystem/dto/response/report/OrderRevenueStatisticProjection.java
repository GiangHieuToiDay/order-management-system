package com.trainning.ordersystem.dto.response.report;

import java.math.BigDecimal;

public interface OrderRevenueStatisticProjection {

    String getPeriod();

    Long getTotalOrders();

    Long getCompletedOrders();

    Long getConfirmedOrders();

    Long getCancelledOrders();

    Long getPendingOrders();

    BigDecimal getTotalRevenue();

    Double getSuccessRate();

    Double getCancellationRate();
}
