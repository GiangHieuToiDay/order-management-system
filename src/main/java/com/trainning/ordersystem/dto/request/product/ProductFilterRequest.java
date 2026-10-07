package com.trainning.ordersystem.dto.request.product;

import com.trainning.ordersystem.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {

    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private ProductStatus status;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 5;

    @Builder.Default
    private String sortBy = "id";

    @Builder.Default
    private String sortDirection = "DESC";
}
