package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.cart.CartItemResponse;
import com.trainning.ordersystem.repository.CartItemRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryCustomImpl implements CartItemRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public List<CartItemResponse> findItemResponsesByCustomerId(Long customerId) {
        String sql = """
            SELECT ci.id AS id,
                   p.id AS productId,
                   p.name AS productName,
                   p.sku AS productSku,
                   p.image_url AS productImage,
                   p.price AS unitPrice,
                   ci.quantity AS quantity
            FROM cart_items ci
            LEFT JOIN products p ON ci.product_id = p.id
            WHERE ci.customer_id = :customerId
            ORDER BY ci.id DESC
        """;

        Query query = entityManager.createNativeQuery(sql, "CartItemResponseMapping");
        query.setParameter("customerId", customerId);

        @SuppressWarnings("unchecked")
        List<CartItemResponse> results = query.getResultList();
        return results;
    }
}
