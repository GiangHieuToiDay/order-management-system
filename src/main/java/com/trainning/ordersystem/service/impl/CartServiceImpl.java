package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.request.cart.AddToCartRequest;
import com.trainning.ordersystem.dto.request.cart.UpdateCartQuantityRequest;
import com.trainning.ordersystem.dto.response.cart.CartItemResponse;
import com.trainning.ordersystem.dto.response.cart.CartResponse;
import com.trainning.ordersystem.entity.CartItem;
import com.trainning.ordersystem.entity.Customer;
import com.trainning.ordersystem.entity.Product;
import com.trainning.ordersystem.mapper.CartMapper;
import com.trainning.ordersystem.repository.CartItemRepository;
import com.trainning.ordersystem.service.CartService;
import com.trainning.ordersystem.specification.CartSpecification;
import com.trainning.ordersystem.specification.CustomerSpecification;
import com.trainning.ordersystem.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartSpecification cartSpecification;
    private final CustomerSpecification customerSpecification;
    private final ProductSpecification productSpecification;
    private final CartMapper cartMapper;

    @Override
    public CartResponse getCart(Long customerId) {
        log.info("Lấy thông tin giỏ hàng của customerId={}", customerId);
        customerSpecification.findCustomerById(customerId);
        return buildCartResponse(customerId);
    }

    @Override
    @Transactional
    public CartResponse addToCart(Long customerId, AddToCartRequest request) {
        log.info("Thêm sản phẩm productId={} vào giỏ hàng của customerId={}", request.getProductId(), customerId);

        Customer customer = customerSpecification.findCustomerById(customerId);
        Product product = productSpecification.getProductById(request.getProductId());

        Optional<CartItem> existingItemOpt = cartItemRepository.findByCustomerIdAndProductId(customerId, request.getProductId());

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            int newQuantity = existingItem.getQuantity() + request.getQuantity();
            productSpecification.validateAvailability(product, newQuantity);
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            productSpecification.validateAvailability(product, request.getQuantity());
            CartItem newItem = new CartItem();
            newItem.setCustomer(customer);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            cartItemRepository.save(newItem);
        }

        return buildCartResponse(customerId);
    }

    @Override
    @Transactional
    public CartResponse updateQuantity(Long customerId, Long cartItemId, UpdateCartQuantityRequest request) {
        log.info("Cập nhật số lượng cartItemId={} thành {} cho customerId={}", cartItemId, request.getQuantity(), customerId);

        CartItem cartItem = cartSpecification.getCartItemById(cartItemId);
        cartSpecification.validateCartItemOwnership(cartItem, customerId);

        if (request.getQuantity() <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            productSpecification.validateAvailability(cartItem.getProduct(), request.getQuantity());
            cartItem.setQuantity(request.getQuantity());
            cartItemRepository.save(cartItem);
        }

        return buildCartResponse(customerId);
    }

    @Override
    @Transactional
    public CartResponse removeCartItem(Long customerId, Long cartItemId) {
        log.info("Xóa cartItemId={} khỏi giỏ hàng của customerId={}", cartItemId, customerId);

        CartItem cartItem = cartSpecification.getCartItemById(cartItemId);
        cartSpecification.validateCartItemOwnership(cartItem, customerId);

        cartItemRepository.delete(cartItem);

        return buildCartResponse(customerId);
    }

    @Override
    @Transactional
    public void clearCart(Long customerId) {
        log.info("Xóa toàn bộ giỏ hàng của customerId={}", customerId);
        customerSpecification.findCustomerById(customerId);
        cartItemRepository.deleteByCustomerId(customerId);
    }

    private CartResponse buildCartResponse(Long customerId) {
        List<CartItem> cartItems = cartItemRepository.findByCustomerId(customerId);
        List<CartItemResponse> itemResponses = cartMapper.toResponseList(cartItems);

        BigDecimal totalAmount = BigDecimal.ZERO;
        if (itemResponses != null && !itemResponses.isEmpty()) {
            totalAmount = itemResponses.stream()
                    .map(CartItemResponse::getSubTotal)
                    .filter(subTotal -> subTotal != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        int totalItems = itemResponses != null ? itemResponses.size() : 0;

        return CartResponse.builder()
                .items(itemResponses)
                .totalAmount(totalAmount)
                .totalItems(totalItems)
                .build();
    }
}
