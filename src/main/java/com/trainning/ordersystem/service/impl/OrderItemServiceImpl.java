package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.order.OrderItemRequest;
import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.entity.*;
import com.trainning.ordersystem.entity.enums.TransactionType;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.repository.InventoryTransactionRepository;
import com.trainning.ordersystem.repository.OrderItemRepository;
import com.trainning.ordersystem.repository.ProductRepository;
import com.trainning.ordersystem.security.SecurityUtils;
import com.trainning.ordersystem.security.userDetails.CustomUserDetails;
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
    private final ProductRepository productRepository;
    private final ProductSpecification productSpecification;
    private final CartItemRepository cartItemRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
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
    public List<OrderItem> createOrderItemsFromCart(Order order, List<Long> cartItemIds) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        Long customerId = userDetails.getCustomerId();

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
        List<OrderItemResponse> items = orderItemRepository.findItemResponsesByOrderId(orderId);
        return items;
    }

    @Override
    public OrderItemResponse getItemById(Long itemId) {
        log.info("Lấy chi tiết OrderItem theo itemId={}", itemId);
        return orderItemRepository.findItemResponseById(itemId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.VALIDATION_ERROR,
                        "Không tìm thấy chi tiết sản phẩm đơn hàng với id = " + itemId
                ));
    }

    @Override
    @Transactional
    public void deductStock(String orderCode, List<OrderItem> items) {
        log.info("Trừ tồn kho từ sự kiện OrderCreatedEvent cho đơn hàng orderCode={}", orderCode);
        if (items == null || items.isEmpty()) {
            return;
        }
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        User actor = userDetails.getUser();

        for (OrderItem item : items) {
            Product product = productSpecification.getProductById(item.getProduct().getId());

            int updatedRows = productRepository.deductStock(product.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new AppException(
                        ErrorCode.INSUFFICIENT_STOCK,
                        "Sản phẩm '" + product.getName() + "' không đủ số lượng tồn kho để xuất hàng"
                );
            }

            int newStock = product.getStockQuantity() - item.getQuantity();

            Long rem = redisService.deductStock(product.getId(), item.getQuantity());
            if (rem == null) {
                redisService.setStock(product.getId(), newStock);
            }

            traceInventoryTransaction(product,TransactionType.OUT,item.getQuantity(), orderCode, actor);
        }

    }

    @Override
    @Transactional
    public void restoreStockForOrderItems(List<OrderItem> items, String orderCode) {
        CustomUserDetails userDetails = SecurityUtils.getCurrentUserDetails();
        User actor = userDetails.getUser();
        log.info("Hoàn trả tồn kho và ghi nhật ký nhập lại cho đơn hàng hủy orderCode={}", orderCode);
        orderItemSpecification.validateItemsNotEmpty(items);

        for (OrderItem item : items) {
            Product product = item.getProduct();
            productRepository.addStock(product.getId(), item.getQuantity());

            int newStock = product.getStockQuantity() + item.getQuantity();

            Long rem = redisService.addStock(product.getId(), item.getQuantity());
            if (rem == null) {
                redisService.setStock(product.getId(), newStock);
            }

            traceInventoryTransaction(product, TransactionType.IN, item.getQuantity(), orderCode ,actor);
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

    private void traceInventoryTransaction(Product product, TransactionType type ,Integer quantity, String orderCode ,User actor){
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProduct(product);
        transaction.setType(type);
        transaction.setQuantity(quantity);
        transaction.setReason("Xuất kho cho đơn hàng " + orderCode);
        transaction.setCreatedBy(actor);
        inventoryTransactionRepository.save(transaction);
    }

}
