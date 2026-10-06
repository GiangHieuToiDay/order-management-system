package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartResponse;
import com.trainning.ordersystem.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @RequestParam(defaultValue = "1") Long customerId) {
        CartResponse response = cartService.getCart(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy giỏ hàng thành công", response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestParam(defaultValue = "1") Long customerId,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(customerId, request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm vào giỏ hàng thành công", response));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @RequestParam(defaultValue = "1") Long customerId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {
        CartResponse response = cartService.updateQuantity(customerId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật số lượng thành công", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @RequestParam(defaultValue = "1") Long customerId,
            @PathVariable Long cartItemId) {
        CartResponse response = cartService.removeCartItem(customerId, cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm khỏi giỏ hàng thành công", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestParam(defaultValue = "1") Long customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sạch giỏ hàng thành công", null));
    }
}
