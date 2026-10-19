package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.order.OrderItemResponse;
import com.trainning.ordersystem.repository.OrderItemRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryCustomImpl implements OrderItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<OrderItemResponse> findItemResponsesByOrderId(Long orderId) {
        String sql = """
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
            ORDER BY oi.id ASC
        """;

        Query query = entityManager.createNativeQuery(sql, "OrderItemResponseMapping");
        query.setParameter("orderId", orderId);

        @SuppressWarnings("unchecked")
        List<OrderItemResponse> results = query.getResultList();
        return results;
    }

    @Override
    public Optional<OrderItemResponse> findItemResponseById(Long id) {
        String sql = """
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
        """;

        Query query = entityManager.createNativeQuery(sql, "OrderItemResponseMapping");
        query.setParameter("id", id);

        @SuppressWarnings("unchecked")
        List<OrderItemResponse> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
