package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.order.OrderItemRequest;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.OrderItem;
import com.trainning.ordersystem.entity.User;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemService {


    List<OrderItem> createOrderItems(Order order, List<OrderItemRequest> itemRequests);

    List<OrderItem> createOrderItemsFromCart(Order order, Long customerId);

    List<OrderItem> createOrderItemsFromCart(Order order, Long customerId, List<Long> cartItemIds);

    List<OrderItemResponse> getItemsByOrderId(Long orderId);

    OrderItemResponse getItemById(Long itemId);

    void deductStock(String orderCode, List<OrderItem> items);

    void restoreStockForOrderItems(List<OrderItem> items, String orderCode, User actor);

    BigDecimal calculateTotalAmount(List<OrderItem> items);
}
