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
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.messaging.event.order.OrderItemEvent;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.repository.OrderRepository;
import com.trainning.ordersystem.repository.OrderRepositoryCustom;
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
    private final OrderRepositoryCustom orderRepositoryCustom;

    @Override
    @Transactional
    public OrderDetailResponse placeOrder( CreateOrderRequest request) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        Long customerId = userDetails.getCustomerId();
        log.info("[ORDER][CREATE] Khách hàng bắt đầu đặt hàng | customerId={}", customerId);


        List<Long> productIds = extractProductIds(customerId, request);

        return redisService.executeWithProductLocks(productIds,
                () -> doPlaceOrder(customerId, request));
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
        log.info("[ORDER][CREATE_SUCCESS] Đặt hàng thành công | orderCode={}", savedOrder.getOrderCode());
        return OrderDetailResponse.from(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderById(Long orderId) {
        log.info("[ORDER][GET_BY_ID] Xem chi tiết đơn hàng | orderId={}", orderId);
        Order order = orderSpecification.getOrderById(orderId);

        orderSpecification.validateOrderOwnership(order);

        return OrderDetailResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderByCode(String orderCode) {
        log.info("[ORDER][GET_BY_CODE] Xem chi tiết đơn hàng | orderCode={}", orderCode);
        Order order = orderSpecification.getOrderByCode(orderCode);

        orderSpecification.validateOrderOwnership(order);

        return OrderDetailResponse.from(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getMyOrders(int page, int size) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        log.info("[ORDER][GET_MY_ORDERS] Lấy lịch sử đơn hàng | customerId={}, page={}, size={}", userDetails.getCustomerId(), page, size);
        customerSpecification.findCustomerById(userDetails.getCustomerId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<OrderSummaryResponse> orderPage = orderRepositoryCustom.findByCustomerId(userDetails.getCustomerId(), pageable);
        return toPageResponse(orderPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getAllOrders(String keyword, OrderStatus status, int page, int size) {
        log.info("[ORDER][SEARCH] Tìm kiếm đơn hàng | keyword={}, status={}, page={}, size={}", keyword, status, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        String statusStr = status != null ? status.name() : null;
        Page<OrderSummaryResponse> orderPage = orderRepositoryCustom.searchOrders(keyword, statusStr, pageable);

        return toPageResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderDetailResponse cancelMyOrder(Long orderId, String reason) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        log.info("[ORDER][CANCEL] Khách hàng yêu cầu hủy đơn hàng | customerId={}, orderId={}", userDetails.getCustomerId(), orderId);

        Order order = orderSpecification.getOrderById(orderId);
        orderSpecification.validateOrderOwnership(order);
        orderSpecification.validateCanCancel(order);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        orderItemService.restoreStockForOrderItems(order.getOrderItems(), order.getOrderCode());

        log.info("[ORDER][CANCEL_SUCCESS] Hủy đơn hàng thành công | orderCode={}, reason={}", savedOrder.getOrderCode(), reason);
        return OrderDetailResponse.from(savedOrder);
    }

    @Override
    @Transactional
    public OrderDetailResponse updateOrderStatus(Long orderId, OrderStatusUpdateRequest request) {
        Order order = orderSpecification.getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = request.getStatus();

        log.info("[ORDER][UPDATE_STATUS] Cập nhật trạng thái đơn hàng | orderId={}, oldStatus={}, newStatus={}", orderId, oldStatus, newStatus);

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

        String normalizedGroupBy = "DAY".equalsIgnoreCase(groupBy) ? "DAY" : "MONTH";

        List<OrderRevenueStatisticResponse> rawRows = orderRepositoryCustom.getOrderRevenueStatistics(
                startDateTime, endDateTime, normalizedGroupBy);

        List<OrderRevenueStatisticResponse> periodList = rawRows.stream()
                .filter(row -> row.getCustomerId() == null)
                .toList();

        Map<String, List<OrderRevenueStatisticResponse>> customerDetailsByPeriod = rawRows.stream()
                .filter(row -> row.getCustomerId() != null)
                .collect(Collectors.groupingBy(OrderRevenueStatisticResponse::getPeriod)); // nhóm các đơn có cùng period li

        for (OrderRevenueStatisticResponse periodStat : periodList) {
            List<OrderRevenueStatisticResponse> details = customerDetailsByPeriod.getOrDefault(
                    periodStat.getPeriod(), Collections.emptyList()
            );
            // Xóa period ở cấp con để JSON trả về không bị lặp lại trường "period"
            details.forEach(d -> d.setPeriod(null));
            periodStat.setCustomerDetails(details);
        }

        return periodList;
    }

    private List<Long> extractProductIds(Long customerId, CreateOrderRequest request) {
        if (request.isFromCart()) {
            log.debug("[ORDER][EXTRACT_IDS] Trích xuất sản phẩm từ giỏ hàng | cartItemIds={}",
                    request.getCartItemIds());

            if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> productIds = cartItemRepository
                    .findByIdInAndCustomerId(request.getCartItemIds(), customerId)
                    .stream()
                    .filter(cartItem -> cartItem.getProduct() != null)
                    .map(cartItem -> cartItem.getProduct().getId())
                    .filter(Objects::nonNull)
                    .toList();

            log.debug("[ORDER][EXTRACT_IDS] Danh sách productId cần khóa: {}", productIds);
            return productIds;
        }

        log.debug("[ORDER][EXTRACT_IDS] Trích xuất sản phẩm mua trực tiếp | itemsCount={}",
                request.getItems() != null ? request.getItems().size() : 0);

        List<Long> productIds = request.getItems() == null
                ? Collections.emptyList()
                : request.getItems().stream()
                .map(OrderItemRequest::getProductId)
                .filter(Objects::nonNull)
                .toList();

        log.debug("[ORDER][EXTRACT_IDS] Danh sách productId cần khóa: {}", productIds);
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

