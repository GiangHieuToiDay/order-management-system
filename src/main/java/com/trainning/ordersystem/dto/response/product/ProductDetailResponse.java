package com.trainning.ordersystem.dto.response.product;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String unit;
    private Integer stockQuantity;

    @JsonProperty("isAvailable")
    private Boolean isAvailable;
    private String description;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor dùng cho JPQL SELECT new (tự động tính isAvailable)
    public ProductDetailResponse(
            Long id,
            String name,
            String sku,
            BigDecimal price,
            String unit,
            Integer stockQuantity,
            String description,
            String imageUrl,
            Long categoryId,
            String categoryName,
            ProductStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
        this.isAvailable = (status == ProductStatus.ACTIVE && stockQuantity != null && stockQuantity > 0);
        this.description = description;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor dùng cho SqlResultSetMapping với String status
    public ProductDetailResponse(
            Long id,
            String name,
            String sku,
            BigDecimal price,
            String unit,
            Integer stockQuantity,
            String description,
            String imageUrl,
            Long categoryId,
            String categoryName,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this(id, name, sku, price, unit, stockQuantity, description, imageUrl, categoryId, categoryName,
                status != null ? ProductStatus.valueOf(status) : null, createdAt, updatedAt);
    }

    public static ProductDetailResponse from(com.trainning.ordersystem.entity.Product product) {
        if (product == null) return null;
        var category = product.getCategory();
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .unit(product.getUnit())
                .stockQuantity(product.getStockQuantity())
                .isAvailable(product.getStatus() == ProductStatus.ACTIVE && product.getStockQuantity() != null && product.getStockQuantity() > 0)
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .categoryId(category != null ? category.getId() : null)
                .categoryName(category != null ? category.getName() : null)
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
