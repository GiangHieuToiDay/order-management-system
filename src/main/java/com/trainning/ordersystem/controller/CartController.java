package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartResponse;
import com.trainning.ordersystem.security.SecurityUtils;
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
            @RequestParam(required = false) Long customerId) {
        Long effectiveCustomerId = resolveCustomerId(customerId);
        CartResponse response = cartService.getCart(effectiveCustomerId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy giỏ hàng thành công", response));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @RequestParam(required = false) Long customerId,
            @Valid @RequestBody AddToCartRequest request) {
        Long effectiveCustomerId = resolveCustomerId(customerId);
        CartResponse response = cartService.addToCart(effectiveCustomerId, request);
        return ResponseEntity.ok(ApiResponse.ok("Thêm vào giỏ hàng thành công", response));
    }

    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateQuantity(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {
        Long effectiveCustomerId = resolveCustomerId(customerId);
        CartResponse response = cartService.updateQuantity(effectiveCustomerId, cartItemId, request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật số lượng thành công", response));
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeCartItem(
            @RequestParam(required = false) Long customerId,
            @PathVariable Long cartItemId) {
        Long effectiveCustomerId = resolveCustomerId(customerId);
        CartResponse response = cartService.removeCartItem(effectiveCustomerId, cartItemId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sản phẩm khỏi giỏ hàng thành công", response));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @RequestParam(required = false) Long customerId) {
        Long effectiveCustomerId = resolveCustomerId(customerId);
        cartService.clearCart(effectiveCustomerId);
        return ResponseEntity.ok(ApiResponse.ok("Xóa sạch giỏ hàng thành công", null));
    }

    /**
     * Khách hàng luôn sử dụng customerId từ token xác thực để tránh IDOR.
     * Admin/Staff có thể truyền customerId để thao tác giỏ hàng cho khách nếu cần.
     */
    private Long resolveCustomerId(Long requestedCustomerId) {
        if (SecurityUtils.isAdminOrStaff() && requestedCustomerId != null) {
            return requestedCustomerId;
        }
        return SecurityUtils.getCurrentCustomerId();
    }
}
