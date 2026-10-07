package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.OrderItem;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.OrderItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@AllArgsConstructor
public class OrderItemSpecification {

    private final OrderItemRepository orderItemRepository;

    public void validateItemsNotEmpty(Collection<?> items) {
        if (items == null || items.isEmpty()) {
            throw new AppException(
                    ErrorCode.EMPTY_ORDER_ITEMS,
                    "Đơn hàng phải có ít nhất 1 sản phẩm"
            );
        }
    }

    public OrderItem getOrderItemById(Long orderItemId) {
        return orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Không tìm thấy chi tiết sản phẩm đơn hàng với id = " + orderItemId
                ));
    }
}
