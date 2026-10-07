package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderItemResponse>> getItemById(@PathVariable Long id) {
        OrderItemResponse response = orderItemService.getItemById(id);
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết mục đơn hàng thành công", response));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> getItemsByOrderId(@PathVariable Long orderId) {
        List<OrderItemResponse> response = orderItemService.getItemsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách mục đơn hàng theo đơn thành công", response));
    }
}
