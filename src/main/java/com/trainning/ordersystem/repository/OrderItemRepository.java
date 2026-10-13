package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.order.OrderItemResponse(
            oi.id,p.id,p.name,p.sku,p.imageUrl,oi.unitPrice,oi.quantity,(oi.unitPrice * oi.quantity)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product p
        WHERE oi.order.id = :orderId
    """)
    List<OrderItemResponse> findItemResponsesByOrderId(@Param("orderId") Long orderId);

    @Query(value = """
        SELECT new com.trainning.ordersystem.dto.response.order.OrderItemResponse(
            oi.id, p.id,p.name,p.sku,p.imageUrl,oi.unitPrice,oi.quantity,(oi.unitPrice * oi.quantity)
        )
        FROM OrderItem oi
        LEFT JOIN oi.product p
        WHERE oi.id = :id
    """)
    Optional<OrderItemResponse> findItemResponseById(@Param("id") Long id);
}
