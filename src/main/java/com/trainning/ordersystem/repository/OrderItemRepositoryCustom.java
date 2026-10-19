package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.order.OrderItemResponse;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepositoryCustom {

    List<OrderItemResponse> findItemResponsesByOrderId(Long orderId);

    Optional<OrderItemResponse> findItemResponseById(Long id);
}
