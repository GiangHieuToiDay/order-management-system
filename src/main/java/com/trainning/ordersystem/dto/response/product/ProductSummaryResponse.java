package com.trainning.ordersystem.dto.response.product;

import com.trainning.ordersystem.entity.enums.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.PersistenceCreator;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSummaryResponse {

    private Long id;
    private String name;
    private String sku;
    private BigDecimal price;
    private String unit;
    private Integer stockQuantity;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private ProductStatus status;


    public ProductSummaryResponse(Long id, String name, String sku, BigDecimal price, String unit, String imageUrl, Long categoryId, String categoryName, ProductStatus status) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.price = price;
        this.unit = unit;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.status = status;
    }


}
