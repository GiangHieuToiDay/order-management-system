package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.response.cart.CartItemResponse;
import com.trainning.ordersystem.entity.CartItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "product.imageUrl", target = "productImage")
    @Mapping(source = "product.price", target = "unitPrice")
    CartItemResponse toResponse(CartItem cartItem);

    List<CartItemResponse> toResponseList(List<CartItem> cartItems);

    @AfterMapping
    default void calculateSubTotal(CartItem cartItem, @MappingTarget CartItemResponse response) {
        if (response.getUnitPrice() != null && response.getQuantity() != null) {
            response.setSubTotal(response.getUnitPrice().multiply(BigDecimal.valueOf(response.getQuantity())));
        }
    }
}
