package com.trainning.ordersystem.service;

import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartResponse;

public interface CartService {


    CartResponse getCart();

    CartResponse addToCart( AddToCartRequest request);

    CartResponse updateQuantity( Long cartItemId, UpdateCartQuantityRequest request);

    CartResponse removeCartItem( Long cartItemId);

    void clearCart();
}
