package com.trainning.ordersystem.entity;

import com.trainning.ordersystem.dto.response.product.ProductDetailResponse;
import com.trainning.ordersystem.dto.response.product.ProductSummaryResponse;
import com.trainning.ordersystem.entity.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products",
        indexes = {
                @Index(name = "IX_products_category_status", columnList = "category_id, status")
        })
@Getter
@Setter
@NoArgsConstructor
@SqlResultSetMapping(
        name = "ProductDetailMapping",
        classes = @ConstructorResult(
                targetClass = ProductDetailResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "sku", type = String.class),
                        @ColumnResult(name = "price", type = BigDecimal.class),
                        @ColumnResult(name = "unit", type = String.class),
                        @ColumnResult(name = "stockQuantity", type = Integer.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "imageUrl", type = String.class),
                        @ColumnResult(name = "categoryId", type = Long.class),
                        @ColumnResult(name = "categoryName", type = String.class),
                        @ColumnResult(name = "status", type = String.class),
                        @ColumnResult(name = "createdAt", type = LocalDateTime.class),
                        @ColumnResult(name = "updatedAt", type = LocalDateTime.class)
                }
        )
)
@SqlResultSetMapping(
        name = "ProductSummaryMapping",
        classes = @ConstructorResult(
                targetClass = ProductSummaryResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "sku", type = String.class),
                        @ColumnResult(name = "price", type = BigDecimal.class),
                        @ColumnResult(name = "unit", type = String.class),
                        @ColumnResult(name = "imageUrl", type = String.class),
                        @ColumnResult(name = "categoryId", type = Long.class),
                        @ColumnResult(name = "categoryName", type = String.class),
                        @ColumnResult(name = "status", type = String.class)
                }
        )
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @org.hibernate.annotations.Nationalized
    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @org.hibernate.annotations.Nationalized
    @Column(length = 50)
    private String unit;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<InventoryTransaction> inventoryTransactions = new ArrayList<>();
}
