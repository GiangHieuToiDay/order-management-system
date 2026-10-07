package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartResponse;

public interface CartService {


    CartResponse getCart(Long customerId);

    CartResponse addToCart(Long customerId, AddToCartRequest request);

    CartResponse updateQuantity(Long customerId, Long cartItemId, UpdateCartQuantityRequest request);

    CartResponse removeCartItem(Long customerId, Long cartItemId);

    void clearCart(Long customerId);
}
