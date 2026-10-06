package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.order.CreateOrderRequest;
import com.trainning.ordersystem.dto.request.order.OrderStatusUpdateRequest;
import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.service.OrderItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderDetailResponse>> placeOrder(
            @RequestParam(defaultValue = "1") Long customerId,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDetailResponse response = orderService.placeOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Đặt hàng thành công", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "false") boolean isAdminOrStaff) {
        OrderDetailResponse response = orderService.getOrderById(id, customerId, isAdminOrStaff);
        return ResponseEntity.ok(ApiResponse.ok("Lấy chi tiết đơn hàng thành công", response));
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByCode(
            @PathVariable String orderCode,
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "false") boolean isAdminOrStaff) {
        OrderDetailResponse response = orderService.getOrderByCode(orderCode, customerId, isAdminOrStaff);
        return ResponseEntity.ok(ApiResponse.ok("Lấy thông tin đơn hàng thành công", response));
    }

    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getMyOrders(
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderSummaryResponse> response = orderService.getMyOrders(customerId, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách đơn hàng thành công", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderSummaryResponse>>> getAllOrders(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<OrderSummaryResponse> response = orderService.getAllOrders(keyword, status, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách tất cả đơn hàng thành công", response));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> cancelMyOrder(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "Khách hàng yêu cầu hủy") String reason) {
        OrderDetailResponse response = orderService.cancelMyOrder(id, customerId, reason);
        return ResponseEntity.ok(ApiResponse.ok("Hủy đơn hàng thành công", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        OrderDetailResponse response = orderService.updateOrderStatus(id, request, null);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật trạng thái đơn hàng thành công", response));
    }

    @GetMapping("/statistics/revenue")
    public ResponseEntity<ApiResponse<List<OrderRevenueStatisticResponse>>> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTH") String groupBy) {
        List<OrderRevenueStatisticResponse> response = orderService.getRevenueStatistics(startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.ok("Thống kê doanh thu và tỷ lệ đơn hàng thành công", response));
    }

    @GetMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<List<OrderItemResponse>>> getOrderItems(@PathVariable Long orderId) {
        List<OrderItemResponse> response = orderItemService.getItemsByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.ok("Lấy danh sách sản phẩm trong đơn hàng thành công", response));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countOrdersByStatus(@RequestParam OrderStatus status) {
        long count = orderService.countOrdersByStatus(status);
        return ResponseEntity.ok(ApiResponse.ok("Đếm số lượng đơn hàng theo trạng thái thành công", count));
    }
}
