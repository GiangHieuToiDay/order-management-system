package com.trainning.ordersystem.entity;

import com.trainning.ordersystem.dto.response.cart.CartItemResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_cart_items_customer_product",
                        columnNames = {"customer_id", "product_id"})
        },
        indexes = {
                @Index(name = "IX_cart_items_customer", columnList = "customer_id")
        })
@Getter
@Setter
@NoArgsConstructor
@SqlResultSetMapping(
        name = "CartItemResponseMapping",
        classes = @ConstructorResult(
                targetClass = CartItemResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "productId", type = Long.class),
                        @ColumnResult(name = "productName", type = String.class),
                        @ColumnResult(name = "productSku", type = String.class),
                        @ColumnResult(name = "productImage", type = String.class),
                        @ColumnResult(name = "unitPrice", type = BigDecimal.class),
                        @ColumnResult(name = "quantity", type = Integer.class)
                }
        )
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
