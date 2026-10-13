package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartResponse;
import com.trainning.ordersystem.security.CustomerContext;
import com.trainning.ordersystem.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final CustomerContext customerContext;

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        CartResponse response = cartService.getCart();
        return ResponseEntity.ok(ApiResponse.ok("Lấy giỏ hàng thành công", response));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm vào giỏ hàng thành công", response));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {
        CartResponse response = cartService.updateQuantity(cartItemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật số lượng thành công", response));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @PathVariable Long cartItemId) {
        CartResponse response = cartService.removeCartItem( cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm khỏi giỏ hàng thành công", response));
    }

    @PreAuthorize("hasAnyRole('CUSTOMER')")
    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok(ApiResponse.ok("Xóa sạch giỏ hàng thành công", null));
    }
}
