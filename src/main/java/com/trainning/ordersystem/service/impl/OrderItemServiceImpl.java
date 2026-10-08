package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.order.OrderItemRequest;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.entity.*;
import com.trainning.ordersystem.entity.enums.TransactionType;
import com.trainning.ordersystem.mapper.OrderMapper;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.repository.InventoryTransactionRepository;
import com.trainning.ordersystem.repository.OrderItemRepository;
import com.trainning.ordersystem.repository.ProductRepository;
import com.trainning.ordersystem.service.OrderItemService;
import com.trainning.ordersystem.service.RedisService;
import com.trainning.ordersystem.specification.OrderItemSpecification;
import com.trainning.ordersystem.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trainning.ordersystem.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderItemSpecification orderItemSpecification;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;
    private final CartItemRepository cartItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final OrderMapper orderMapper;
    private final RedisService redisService;

    @Override
    public List<OrderItem> createOrderItems(Order order, List<OrderItemRequest> itemRequests) {
        log.info("Khởi tạo danh sách OrderItem từ request đặt hàng");
        orderItemSpecification.validateItemsNotEmpty(itemRequests);

        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest request : itemRequests) {
            Product product = productSpecification.getProductById(request.getProductId());
            productSpecification.validateAvailability(product, request.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(request.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    @Override
    public List<OrderItem> createOrderItemsFromCart(Order order, Long customerId) {
        return createOrderItemsFromCart(order, customerId, null);
    }

    @Override
    public List<OrderItem> createOrderItemsFromCart(Order order, Long customerId, List<Long> cartItemIds) {
        log.info("Khởi tạo danh sách OrderItem từ giỏ hàng cho customerId={}, cartItemIds={}", customerId, cartItemIds);

        List<CartItem> cartItems = cartItemRepository.findByCustomerIdAndOptionalIds(customerId, cartItemIds);
        orderItemSpecification.validateItemsNotEmpty(cartItems);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            productSpecification.validateAvailability(product, cartItem.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);
        }

        return orderItems;
    }

    @Override
    public List<OrderItemResponse> getItemsByOrderId(Long orderId) {
        log.info("Lấy danh sách OrderItem theo orderId={}", orderId);
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return mapToResponseList(items);
    }

    @Override
    public OrderItemResponse getItemById(Long itemId) {
        log.info("Lấy chi tiết OrderItem theo itemId={}", itemId);
        OrderItem item = orderItemSpecification.getOrderItemById(itemId);
        return mapToResponse(item);
    }

    @Override
    @Transactional
    public void deductStock(String orderCode, List<OrderItem> items) {
        log.info("Trừ tồn kho từ sự kiện OrderCreatedEvent cho đơn hàng orderCode={}", orderCode);
        if (items == null || items.isEmpty()) {
            return;
        }

        User actor = null;
        if (items.get(0).getOrder() != null && items.get(0).getOrder().getCustomer() != null) {
            actor = items.get(0).getOrder().getCustomer().getUser();
        }

        if (actor == null) {
            Optional<Order> orderOpt = orderRepository.findByOrderCode(orderCode);
            if (orderOpt.isPresent() && orderOpt.get().getCustomer() != null) {
                actor = orderOpt.get().getCustomer().getUser();
            }
        }

        for (OrderItem item : items) {
            Product product = productSpecification.getProductById(item.getProduct().getId());
            int newStock = product.getStockQuantity() - item.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);

            Long rem = redisService.deductStock(product.getId(), item.getQuantity());
            if (rem == null) {
                redisService.setStock(product.getId(), newStock);
            }

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setType(TransactionType.OUT);
            transaction.setQuantity(item.getQuantity());
            transaction.setReason("Xuất kho cho đơn hàng " + orderCode);
            transaction.setCreatedBy(actor);
            inventoryTransactionRepository.save(transaction);
        }

    }

    @Override
    @Transactional
    public void restoreStockForOrderItems(List<OrderItem> items, String orderCode, User actor) {
        log.info("Hoàn trả tồn kho và ghi nhật ký nhập lại cho đơn hàng hủy orderCode={}", orderCode);
        orderItemSpecification.validateItemsNotEmpty(items);

        User transactionUser = resolveTransactionActor(items, actor);

        for (OrderItem item : items) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() + item.getQuantity();
            product.setStockQuantity(newStock);
            productRepository.save(product);

            Long rem = redisService.addStock(product.getId(), item.getQuantity());
            if (rem == null) {
                redisService.setStock(product.getId(), newStock);
            }

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setProduct(product);
            transaction.setType(TransactionType.IN);
            transaction.setReason("Hoàn kho do hủy đơn hàng " + orderCode);
            transaction.setCreatedBy(transactionUser);
            transaction.setQuantity(item.getQuantity());
            inventoryTransactionRepository.save(transaction);
        }
    }

    @Override
    public BigDecimal calculateTotalAmount(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        if (items == null || items.isEmpty()) {
            return total;
        }

        for (OrderItem item : items) {
            BigDecimal unitPrice = item.getUnitPrice();
            Integer quantity = item.getQuantity();

            if (unitPrice != null && quantity != null) {
                total = total.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            }
        }

        return total;
    }

    private User resolveTransactionActor(List<OrderItem> items, User actor) {
        if (actor != null) {
            return actor;
        }
        if (items != null && !items.isEmpty()
                && items.get(0).getOrder() != null
                && items.get(0).getOrder().getCustomer() != null) {
            return items.get(0).getOrder().getCustomer().getUser();
        }
        return null;
    }

    private List<OrderItemResponse> mapToResponseList(List<OrderItem> items) {
        List<OrderItemResponse> responses = orderMapper.toItemResponseList(items);
        if (responses != null && items != null) {
            for (int i = 0; i < responses.size() && i < items.size(); i++) {
                populateSubTotal(responses.get(i), items.get(i));
            }
        }
        return responses;
    }

    private OrderItemResponse mapToResponse(OrderItem item) {
        OrderItemResponse response = orderMapper.toItemResponse(item);
        if (response != null && item != null) {
            populateSubTotal(response, item);
        }
        return response;
    }

    private void populateSubTotal(OrderItemResponse response, OrderItem item) {
        if (response != null && response.getSubTotal() == null
                && item != null && item.getUnitPrice() != null && item.getQuantity() != null) {
            response.setSubTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
    }
}
