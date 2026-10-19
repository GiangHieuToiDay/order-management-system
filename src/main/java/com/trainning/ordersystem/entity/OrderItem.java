package com.trainning.ordersystem.entity;

import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
        indexes = {
                @Index(name = "IX_order_items_order", columnList = "order_id"),
                @Index(name = "IX_order_items_product", columnList = "product_id")
        })
@Getter
@Setter
@NoArgsConstructor
@SqlResultSetMapping(
        name = "OrderItemResponseMapping",
        classes = @ConstructorResult(
                targetClass = OrderItemResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "productId", type = Long.class),
                        @ColumnResult(name = "productName", type = String.class),
                        @ColumnResult(name = "productSku", type = String.class),
                        @ColumnResult(name = "productImage", type = String.class),
                        @ColumnResult(name = "unitPrice", type = BigDecimal.class),
                        @ColumnResult(name = "quantity", type = Integer.class),
                        @ColumnResult(name = "subTotal", type = BigDecimal.class)
                }
        )
)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;
}
