package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.dto.request.order.CreateOrderRequest;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderSpecification {

    private final OrderRepository orderRepository;

    public Order buildInitialOrder(Customer customer, CreateOrderRequest request, String orderCode) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderCode(orderCode);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        return order;
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Không tìm thấy đơn hàng với id = " + orderId
                ));
    }

    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(
                        ErrorCode.ORDER_NOT_FOUND,
                        "Không tìm thấy đơn hàng với mã: " + orderCode
                ));
    }

    public void validateOrderOwnership(Order order, Long customerId) {
        if (order.getCustomer() == null || !order.getCustomer().getId().equals(customerId)) {
            throw new AppException(
                    ErrorCode.ACCESS_DENIED,
                    "Bạn không có quyền xem hoặc thao tác trên đơn hàng này"
            );
        }
    }

    public void validateCanCancel(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(
                    ErrorCode.CANNOT_CANCEL_ORDER,
                    "Chỉ có thể hủy đơn hàng khi đang ở trạng thái PENDING (Chờ xử lý)"
            );
        }
    }

    public void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.COMPLETED || currentStatus == OrderStatus.CANCELLED) {
            throw new AppException(
                    ErrorCode.INVALID_ORDER_STATUS,
                    "Đơn hàng đã " + currentStatus + ", không thể thay đổi trạng thái nữa"
            );
        }

        if (currentStatus == OrderStatus.PENDING) {
            if (newStatus != OrderStatus.CONFIRMED && newStatus != OrderStatus.CANCELLED) {
                throw new AppException(
                        ErrorCode.INVALID_ORDER_STATUS,
                        "Đơn hàng PENDING chỉ có thể chuyển sang CONFIRMED hoặc CANCELLED"
                );
            }
        } else if (currentStatus == OrderStatus.CONFIRMED) {
            if (newStatus != OrderStatus.COMPLETED && newStatus != OrderStatus.CANCELLED) {
                throw new AppException(
                        ErrorCode.INVALID_ORDER_STATUS,
                        "Đơn hàng CONFIRMED chỉ có thể chuyển sang COMPLETED hoặc CANCELLED"
                );
            }
        }
    }
}
