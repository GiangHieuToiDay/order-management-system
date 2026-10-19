package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.cart.CartItemResponse;

import java.util.List;

public interface CartItemRepositoryCustom {

    List<CartItemResponse> findItemResponsesByCustomerId(Long customerId);
}
