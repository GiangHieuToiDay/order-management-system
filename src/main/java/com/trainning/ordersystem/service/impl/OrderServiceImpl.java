package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.order.CreateOrderRequest;
import com.trainning.ordersystem.dto.request.order.OrderStatusUpdateRequest;
import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.entity.*;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.entity.enums.PaymentMethod;
import com.trainning.ordersystem.mapper.OrderMapper;
import com.trainning.ordersystem.dto.request.order.OrderItemRequest;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticProjection;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.messaging.event.order.OrderItemEvent;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.repository.OrderRepository;
import com.trainning.ordersystem.service.CartService;
import com.trainning.ordersystem.service.OrderItemService;
import com.trainning.ordersystem.service.OrderService;
import com.trainning.ordersystem.service.RedisService;
import com.trainning.ordersystem.specification.CustomerSpecification;
import com.trainning.ordersystem.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSpecification orderSpecification;
    private final CustomerSpecification customerSpecification;
    private final OrderItemService orderItemService;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderDetailResponse placeOrder(Long customerId, CreateOrderRequest request) {
        log.info("Khách hàng customerId={} tiến hành đặt hàng", customerId);

        List<Long> productIds = extractProductIds(customerId, request);

        return redisService.executeWithProductLocks(productIds, () -> doPlaceOrder(customerId,request));
    }

    private OrderDetailResponse doPlaceOrder(Long customerId, CreateOrderRequest request) {
        Customer customer = customerSpecification.findCustomerById(customerId);

        Order order = orderSpecification.buildInitialOrder(customer, request, generateOrderCode());

        List<OrderItem> orderItems;
        if (request.isFromCart()) {
            orderItems = orderItemService.createOrderItemsFromCart(order, customerId, request.getCartItemIds());
        } else {
            orderItems = orderItemService.createOrderItems(order, request.getItems());
        }

        order.setOrderItems(orderItems);
        BigDecimal totalAmount = orderItemService.calculateTotalAmount(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItemsList = savedOrder.getOrderItems();
        orderItemService.deductStock(savedOrder.getOrderCode(), orderItemsList);

        if (savedOrder.getPaymentMethod() == PaymentMethod.BANK_TRANSFER) {
            publishEventOrder(savedOrder);
        }



        if (request.isFromCart()) {
            if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
                cartItemRepository.deleteAllById(request.getCartItemIds());
            } else {
                cartService.clearCart(customerId);
            }
        }

        log.info("Đặt hàng thành công với mã đơn: {}", savedOrder.getOrderCode());
        return orderMapper.toDetailResponse(savedOrder);
    }

    @Override
    public OrderDetailResponse getOrderById(Long orderId, Long currentCustomerId, boolean isAdminOrStaff) {
        log.info("Xem chi tiết đơn hàng orderId={}", orderId);
        Order order = orderSpecification.getOrderById(orderId);

        if (!isAdminOrStaff) {
            orderSpecification.validateOrderOwnership(order, currentCustomerId);
        }

        return orderMapper.toDetailResponse(order);
    }

    @Override
    public OrderDetailResponse getOrderByCode(String orderCode, Long currentCustomerId, boolean isAdminOrStaff) {
        log.info("Xem chi tiết đơn hàng theo orderCode={}", orderCode);
        Order order = orderSpecification.getOrderByCode(orderCode);

        if (!isAdminOrStaff) {
            orderSpecification.validateOrderOwnership(order, currentCustomerId);
        }

        return orderMapper.toDetailResponse(order);
    }

    @Override
    public PageResponse<OrderSummaryResponse> getMyOrders(Long customerId, int page, int size) {
        log.info("Lấy lịch sử đơn hàng của customerId={}, page={}, size={}", customerId, page, size);
        customerSpecification.findCustomerById(customerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        return toPageResponse(orderPage);
    }

    @Override
    public PageResponse<OrderSummaryResponse> getAllOrders(String keyword, OrderStatus status, int page, int size) {
        log.info("Tìm kiếm đơn hàng: keyword={}, status={}, page={}, size={}", keyword, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Order> orderPage = orderRepository.searchOrders(keyword, status, pageable);

        return toPageResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelMyOrder(Long orderId, Long customerId, String reason) {
        log.info("Khách hàng customerId={} yêu cầu hủy đơn hàng orderId={}", customerId, orderId);

        Order order = orderSpecification.getOrderById(orderId);
        orderSpecification.validateOrderOwnership(order, customerId);
        orderSpecification.validateCanCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        User actor = order.getCustomer() != null ? order.getCustomer().getUser() : null;
        orderItemService.restoreStockForOrderItems(order.getOrderItems(), order.getOrderCode(), actor);

        log.info("Hủy đơn hàng {} thành công. Lý do: {}", savedOrder.getOrderCode(), reason);
        return orderMapper.toDetailResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request, User actor) {
        log.info("Cập nhật trạng thái đơn hàng orderId={} thành {}", orderId, request.getStatus());

        Order order = orderSpecification.getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();


        orderSpecification.validateStatusTransition(oldStatus, newStatus);

        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);

        if (savedOrder.getPaymentMethod() == PaymentMethod.COD && newStatus == OrderStatus.COMPLETED) {
            publishEventOrder(savedOrder);
        }


        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            orderItemService.restoreStockForOrderItems(order.getOrderItems(), order.getOrderCode(), actor);
        }

        return orderMapper.toDetailResponse(savedOrder);
    }

    @Override
    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    private String generateOrderCode() {
        return "ORD" + System.currentTimeMillis();
    }


    @Override
    public List<OrderRevenueStatisticResponse> getRevenueStatistics(LocalDate startDate, LocalDate endDate, String groupBy) {
        log.info("Thống kê doanh thu: startDate={}, endDate={}, groupBy={}", startDate, endDate, groupBy);

        LocalDateTime startDateTime = (startDate != null)
                ? startDate.atStartOfDay()
                : LocalDateTime.now().minusMonths(6).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        LocalDateTime endDateTime = (endDate != null)
                ? endDate.atTime(LocalTime.MAX)
                : LocalDateTime.now();

        String normalizedGroupBy = (groupBy != null && groupBy.equalsIgnoreCase("DAY")) ? "DAY" : "MONTH";

        List<OrderRevenueStatisticProjection> projections = orderRepository.getOrderRevenueStatistics(
                startDateTime, endDateTime, normalizedGroupBy
        );

        return projections.stream()
                .map(p -> OrderRevenueStatisticResponse.builder()
                        .period(p.getPeriod())
                        .totalOrders(p.getTotalOrders())
                        .completedOrders(p.getCompletedOrders())
                        .confirmedOrders(p.getConfirmedOrders())
                        .cancelledOrders(p.getCancelledOrders())
                        .pendingOrders(p.getPendingOrders())
                        .totalRevenue(p.getTotalRevenue())
                        .successRate(p.getSuccessRate())
                        .cancellationRate(p.getCancellationRate())
                        .build())
                .toList();
    }

    private List<Long> extractProductIds(Long customerId, CreateOrderRequest request) {
        List<Long> productIds = new ArrayList<>();
        if (request.isFromCart()) {
            List<CartItem> cartItems = cartItemRepository.findByCustomerIdAndOptionalIds(customerId, request.getCartItemIds());
            if (cartItems != null) {
                for (CartItem item : cartItems) {
                    if (item.getProduct() != null && item.getProduct().getId() != null) {
                        productIds.add(item.getProduct().getId());
                    }
                }
            }
        } else if (request.getItems() != null) {
            for (OrderItemRequest item : request.getItems()) {
                if (item.getProductId() != null) {
                    productIds.add(item.getProductId());
                }
            }
        }
        return productIds;
    }

    private PageResponse<OrderSummaryResponse> toPageResponse(
            Page<Order> orderPage
    ) {
        return PageResponse.<OrderSummaryResponse>builder()
                .content(orderMapper.toSummaryResponseList(orderPage.getContent()))
                .pageNumber(orderPage.getNumber())
                .pageSize(orderPage.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .isFirst(orderPage.isFirst())
                .isLast(orderPage.isLast())
                .build();
    }

    private void publishEventOrder(Order order){
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                order.getId(),
                order.getCustomer().getId(),
                order.getOrderCode(),
                order.getOrderItems().stream()
                        .map(item -> new OrderItemEvent(item.getId(), item.getQuantity()))
                        .toList()
        );
        eventPublisher.publishEvent(orderCreatedEvent);
    }

}

