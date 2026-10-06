package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.OrderItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.imageUrl", target = "productImage")
    @Mapping(target = "subTotal", ignore = true)
    OrderItemResponse toItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toItemResponseList(List<OrderItem> orderItems);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.user.fullName", target = "customerName")
    @Mapping(source = "customer.user.fullName", target = "recipientName")
    @Mapping(target = "totalItems", ignore = true)
    OrderSummaryResponse toSummaryResponse(Order order);

    List<OrderSummaryResponse> toSummaryResponseList(List<Order> orders);

    @Mapping(source = "customer.id", target = "customerId")
    @Mapping(source = "customer.user.fullName", target = "customerName")
    @Mapping(source = "customer.user.email", target = "customerEmail")
    @Mapping(source = "customer.user.phone", target = "customerPhone")
    @Mapping(source = "customer.user.fullName", target = "recipientName")
    @Mapping(source = "customer.user.phone", target = "recipientPhone")
    @Mapping(target = "note", ignore = true)
    @Mapping(source = "orderItems", target = "items")
    OrderDetailResponse toDetailResponse(Order order);

    @AfterMapping
    default void calculateTotalItems(Order order, @MappingTarget OrderSummaryResponse response) {
        if (order.getOrderItems() != null) {
            response.setTotalItems(order.getOrderItems().size());
        }
    }

    @AfterMapping
    default void calculateSubTotal(OrderItem orderItem, @MappingTarget OrderItemResponse response) {
        if (orderItem.getUnitPrice() != null && orderItem.getQuantity() != null) {
            response.setSubTotal(orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }
    }
}
