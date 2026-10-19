package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @NativeQuery(
        value = """
            SELECT oi.id AS id,
                   p.id AS productId,
                   p.name AS productName,
                   p.sku AS productSku,
                   p.image_url AS productImage,
                   oi.unit_price AS unitPrice,
                   oi.quantity AS quantity,
                   (oi.unit_price * oi.quantity) AS subTotal
            FROM order_items oi
            LEFT JOIN products p ON oi.product_id = p.id
            WHERE oi.order_id = :orderId
        """,
        sqlResultSetMapping = "OrderItemResponseMapping"
    )
    List<OrderItemResponse> findItemResponsesByOrderId(@Param("orderId") Long orderId);

    @NativeQuery(
        value = """
            SELECT oi.id AS id,
                   p.id AS productId,
                   p.name AS productName,
                   p.sku AS productSku,
                   p.image_url AS productImage,
                   oi.unit_price AS unitPrice,
                   oi.quantity AS quantity,
                   (oi.unit_price * oi.quantity) AS subTotal
            FROM order_items oi
            LEFT JOIN products p ON oi.product_id = p.id
            WHERE oi.id = :id
        """,
        sqlResultSetMapping = "OrderItemResponseMapping"
    )
    Optional<OrderItemResponse> findItemResponseById(@Param("id") Long id);
}
