package com.trainning.ordersystem.entity;

import com.trainning.ordersystem.dto.response.inventory.InventoryTransactionResponse;
import com.trainning.ordersystem.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions",
        indexes = {
                @Index(name = "IX_inv_trans_product", columnList = "product_id"),
                @Index(name = "IX_inv_trans_created_at", columnList = "created_at")
        })
@Getter
@Setter
@NoArgsConstructor
@SqlResultSetMapping(
        name = "InventoryTransactionResponseMapping",
        classes = @ConstructorResult(
                targetClass = InventoryTransactionResponse.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "productId", type = Long.class),
                        @ColumnResult(name = "productName", type = String.class),
                        @ColumnResult(name = "productSku", type = String.class),
                        @ColumnResult(name = "type", type = String.class),
                        @ColumnResult(name = "quantity", type = Integer.class),
                        @ColumnResult(name = "note", type = String.class),
                        @ColumnResult(name = "createdBy", type = Long.class),
                        @ColumnResult(name = "createdByName", type = String.class),
                        @ColumnResult(name = "createdAt", type = LocalDateTime.class)
                }
        )
)
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 500)
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
