package com.trainning.ordersystem.specification;

import com.trainning.ordersystem.entity.CartItem;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.repository.CartItemRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CartSpecification {

    private final CartItemRepository cartItemRepository;

    public CartItem getCartItemById(Long cartItemId) {
        return cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new AppException(
                        ErrorCode.CART_ITEM_NOT_FOUND,
                        "Không tìm thấy sản phẩm trong giỏ hàng với id = " + cartItemId
                ));
    }

    public void validateCartItemOwnership(CartItem cartItem, Long customerId) {
        if (cartItem.getCustomer() == null || !cartItem.getCustomer().getId().equals(customerId)) {
            throw new AppException(
                    ErrorCode.ACCESS_DENIED,
                    "Bạn không có quyền thao tác trên sản phẩm này trong giỏ hàng"
            );
        }
    }
}
