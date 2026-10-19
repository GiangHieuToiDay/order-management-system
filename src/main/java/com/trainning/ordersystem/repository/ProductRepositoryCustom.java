package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;

public interface ProductRepositoryCustom {

    Optional<ProductDetailResponse> findDetailById(Long id);

    Page<ProductSummaryResponse> searchProductSummaries(
            String keyword,
            Long categoryId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );
}