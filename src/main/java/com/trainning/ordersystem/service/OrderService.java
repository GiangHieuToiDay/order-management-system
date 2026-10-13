package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.order.CreateOrderRequest;
import com.trainning.ordersystem.dto.request.order.OrderStatusUpdateRequest;
import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.OrderStatus;

import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderDetailResponse placeOrder(CreateOrderRequest request);

    OrderDetailResponse getOrderById(Long orderId);

    OrderDetailResponse getOrderByCode(String orderCode);

    PageResponse<OrderSummaryResponse> getMyOrders( int page, int size);

    PageResponse<OrderSummaryResponse> getAllOrders(String keyword, OrderStatus status, int page, int size);

    OrderDetailResponse cancelMyOrder(Long orderId, String reason);

    OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request);

    long countOrdersByStatus(OrderStatus status);

    List<OrderRevenueStatisticResponse> getRevenueStatistics(LocalDate startDate, LocalDate endDate, String groupBy);

    OrderDetailResponse getOrderByIdInternal(Long orderId);
}
