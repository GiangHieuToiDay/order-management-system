package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.common.PageResponse;
import com.trainning.ordersystem.dto.request.order.CreateOrderRequest;
import com.trainning.ordersystem.dto.request.order.OrderStatusUpdateRequest;
import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.entity.*;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.entity.enums.PaymentMethod;
import com.trainning.ordersystem.dto.request.order.OrderItemRequest;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticProjection;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.dto.response.report.PeriodOrderDetailResponse;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.messaging.event.order.OrderItemEvent;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.repository.OrderRepository;
import com.trainning.ordersystem.security.SecurityUtils;
import com.trainning.ordersystem.security.userDetails.CustomUserDetails;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderSpecification orderSpecification;
    private final CustomerSpecification customerSpecification;
    private final OrderItemService orderItemService;
    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final RedisService redisService;
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate orderTransactionTemplate;

    @Override
    public OrderDetailResponse placeOrder( CreateOrderRequest request) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        Long customerId = userDetails.getCustomerId();
        log.info("Khách hàng customerId={} tiến hành đặt hàng", customerId);


        List<Long> productIds = extractProductIds(customerId, request);

        return redisService.executeWithProductLocks(productIds,
                () -> orderTransactionTemplate.execute(status -> doPlaceOrder(customerId, request)));
    }

    private OrderDetailResponse doPlaceOrder(Long customerId, CreateOrderRequest request) {
        Customer customer = customerSpecification.findCustomerById(customerId);

        Order order = orderSpecification.buildInitialOrder(customer, request, generateOrderCode());

        List<OrderItem> orderItems;
        if (request.isFromCart()) {
            orderItems = orderItemService.createOrderItemsFromCart(order, request.getCartItemIds());
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
                cartService.clearCart();
            }
        }
        log.info("Đặt hàng thành công với mã đơn: {}", savedOrder.getOrderCode());
        return OrderDetailResponse.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long orderId) {
        log.info("Xem chi tiết đơn hàng orderId={}", orderId);
        Order order = orderSpecification.getOrderById(orderId);

        orderSpecification.validateOrderOwnership(order);

        return OrderDetailResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByCode(String orderCode) {
        log.info("Xem chi tiết đơn hàng theo orderCode={}", orderCode);
        Order order = orderSpecification.getOrderByCode(orderCode);

        orderSpecification.validateOrderOwnership(order);

        return OrderDetailResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(int page, int size) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        log.info("Lấy lịch sử đơn hàng của customerId={}, page={}, size={}", userDetails.getUser(), page, size);
        customerSpecification.findCustomerById(userDetails.getCustomerId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<OrderSummaryResponse> orderPage = orderRepository.findByCustomerId(userDetails.getCustomerId(), pageable);
        return toPageResponse(orderPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAllOrders(String keyword, OrderStatus status, int page, int size) {
        log.info("Tìm kiếm đơn hàng: keyword={}, status={}, page={}, size={}", keyword, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<OrderSummaryResponse> orderPage = orderRepository.searchOrders(keyword, status, pageable);

        return toPageResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelMyOrder(Long orderId, String reason) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        log.info("Khách hàng customerId={} yêu cầu hủy đơn hàng orderId={}", userDetails.getUserId(), orderId);

        Order order = orderSpecification.getOrderById(orderId);
        orderSpecification.validateOrderOwnership(order);
        orderSpecification.validateCanCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        orderItemService.restoreStockForOrderItems(order.getOrderItems(), order.getOrderCode());

        log.info("Hủy đơn hàng {} thành công. Lý do: {}", savedOrder.getOrderCode(), reason);
        return OrderDetailResponse.from(savedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        log.info("Cập nhật trạng thái đơn hàng orderId={} thành {}", orderId, request.getStatus());
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();

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
            orderItemService.restoreStockForOrderItems(order.getOrderItems(), order.getOrderCode());
        }

        return OrderDetailResponse.from(savedOrder);
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

        List<PeriodOrderDetailResponse> orderDetails = orderRepository.findPeriodOrderDetails(
                startDateTime, endDateTime, List.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED)
        );

        DateTimeFormatter formatter = "DAY".equals(normalizedGroupBy)
                ? DateTimeFormatter.ofPattern("yyyy-MM-dd")
                : DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, List<PeriodOrderDetailResponse>> ordersByPeriod = new HashMap<>();

        for (PeriodOrderDetailResponse order : orderDetails) {
            if (order.getCreatedAt() == null) {
                continue;
            }

            String date = order.getCreatedAt().format(formatter);

            if (!ordersByPeriod.containsKey(date)) {
                ordersByPeriod.put(date, new ArrayList<>());
            }

            ordersByPeriod.get(date).add(order);
        }

        List<OrderRevenueStatisticResponse> responses = new ArrayList<>();

        for (OrderRevenueStatisticProjection p : projections) {
            OrderRevenueStatisticResponse response =
                    OrderRevenueStatisticResponse.builder()
                            .period(p.getPeriod())
                            .totalOrders(p.getTotalOrders())
                            .completedOrders(p.getCompletedOrders())
                            .confirmedOrders(p.getConfirmedOrders())
                            .cancelledOrders(p.getCancelledOrders())
                            .pendingOrders(p.getPendingOrders())
                            .totalRevenue(p.getTotalRevenue())
                            .successRate(p.getSuccessRate())
                            .cancellationRate(p.getCancellationRate())
                            .orders(ordersByPeriod.getOrDefault(
                                    p.getPeriod(), Collections.emptyList()))
                            .build();

            responses.add(response);
        }

        return responses;
    }

    private List<Long> extractProductIds(Long customerId, CreateOrderRequest request) {
        List<Long> productIds = new ArrayList<>();
        if (request.isFromCart()) {
            // Trường hợp đặt hàng từ giỏ hàng
            log.info("-------------------------------------Mua Hàng Từ Cart---------------------------------------");
            List<CartItem> cartItems = cartItemRepository.findByCustomerIdAndOptionalIds( customerId, request.getCartItemIds());

            if (cartItems == null) {
                return productIds;
            }

            for (CartItem cartItem : cartItems) {
                if (cartItem.getProduct() == null) {
                    continue;
                }
                Long productId = cartItem.getProduct().getId();
                if (productId != null) {
                    productIds.add(productId);
                }
            }

        } else {
            // Trường hợp mua hàng trực tiếp
            log.info("-------------------------------------Mua Hàng Trực Tiếp---------------------------------------");
            List<OrderItemRequest> items = request.getItems();

            if (items == null) {
                return productIds;
            }

            for (OrderItemRequest item : items) {
                Long productId = item.getProductId();
                if (productId != null) {
                    productIds.add(productId);
                }
            }

        }

        return productIds;
    }

    private PageResponse<OrderSummaryResponse> toPageResponse(
            Page<OrderSummaryResponse> orderPage
    ) {
        return PageResponse.<OrderSummaryResponse>builder()
                .content(orderPage.stream().toList())
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

    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByIdInternal(Long orderId) {
        Order order = orderSpecification.getOrderById(orderId);
        return OrderDetailResponse.from(order);
    }



}

