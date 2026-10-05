package com.trainning.ordersystem.dto.response.order;

import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private Long id;
    private String orderCode;

    // Khách hàng đặt
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    // Thông tin nhận hàng
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;

    // Thanh toán & trạng thái
    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Chi tiết từng dòng sản phẩm
    private List<OrderItemResponse> items;
}
