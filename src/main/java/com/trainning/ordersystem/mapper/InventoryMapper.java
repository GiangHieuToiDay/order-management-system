package com.trainning.ordersystem.mapper;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.entity.InventoryTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sku", target = "productSku")
    @Mapping(source = "createdBy.id", target = "createdBy")
    @Mapping(source = "createdBy.fullName", target = "createdByName")
    @Mapping(source = "reason", target = "note")
    @Mapping(target = "balanceAfter", ignore = true)
    @Mapping(target = "referenceCode", ignore = true)
    InventoryTransactionResponse toResponse(InventoryTransaction transaction);

    List<InventoryTransactionResponse> toResponseList(List<InventoryTransaction> transactions);
}
