package com.trainning.ordersystem.repository;


import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {

    Page<OrderSummaryResponse> findByCustomerId(Long customerId, Pageable pageable);

    Page<OrderSummaryResponse> searchOrders(String keyword, String status, Pageable pageable);

    List<OrderRevenueStatisticResponse> getOrderRevenueStatistics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String groupBy
    );
}
