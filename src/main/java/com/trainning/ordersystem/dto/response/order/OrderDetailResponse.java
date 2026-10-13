package com.trainning.ordersystem.dto.response.order;

import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.OrderItem;
import com.trainning.ordersystem.entity.User;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.entity.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {

    private Long id;
    private String orderCode;

    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;

    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String note;

    private BigDecimal totalAmount;
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;

    public static OrderDetailResponse from(Order order, List<OrderItemResponse> items) {
        if (order == null) return null;

        Customer customer = order.getCustomer();
        User user = (customer != null) ? customer.getUser() : null;

        return OrderDetailResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .customerId(customer != null ? customer.getId() : null)
                .customerName(user != null ? user.getFullName() : null)
                .customerEmail(user != null ? user.getEmail() : null)
                .customerPhone(user != null ? user.getPhone() : null)
                .recipientName(user != null ? user.getFullName() : null)
                .recipientPhone(user != null ? user.getPhone() : null)
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .build();
    }

    public static OrderDetailResponse from(Order order) {
        if (order == null) return null;

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                OrderItemResponse response = OrderItemResponse.from(item);
                itemResponses.add(response);
            }
        }

        return from(order, itemResponses);
    }

    // Constructor dùng cho JPQL SELECT new
    public OrderDetailResponse(
            Long id,
            String orderCode,
            Long customerId,
            String customerName,
            String customerEmail,
            String customerPhone,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            String note,
            BigDecimal totalAmount,
            OrderStatus status,
            PaymentMethod paymentMethod,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.orderCode = orderCode;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


}
