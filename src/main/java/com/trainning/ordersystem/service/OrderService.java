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

    OrderDetailResponse placeOrder(Long customerId, CreateOrderRequest request);

    OrderDetailResponse getOrderById(Long orderId, Long currentCustomerId, boolean isAdminOrStaff);

    OrderDetailResponse getOrderByCode(String orderCode, Long currentCustomerId, boolean isAdminOrStaff);

    PageResponse<OrderSummaryResponse> getMyOrders(Long customerId, int page, int size);

    PageResponse<OrderSummaryResponse> getAllOrders(String keyword, OrderStatus status, int page, int size);

    OrderDetailResponse cancelMyOrder(Long orderId, Long customerId, String reason);

    OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, User actor);

    long countOrdersByStatus(OrderStatus status);

    List<OrderRevenueStatisticResponse> getRevenueStatistics(LocalDate startDate, LocalDate endDate, String groupBy);
}
