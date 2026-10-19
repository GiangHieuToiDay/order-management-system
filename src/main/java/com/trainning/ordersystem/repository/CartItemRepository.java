package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.cart.CartItemResponse;
import com.trainning.ordersystem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);

    @NativeQuery(
        value = """
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
        """,
        sqlResultSetMapping = "CartItemResponseMapping"
    )
    List<CartItemResponse> findItemResponsesByCustomerId(@Param("customerId") Long customerId);

    List<CartItem> findByIdInAndCustomerId(Collection<Long> ids, Long customerId);
    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);
    void deleteByCustomerId(Long customerId);
}
