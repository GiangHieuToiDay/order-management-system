package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCustomerId(Long customerId);
    List<CartItem> findByIdInAndCustomerId(Collection<Long> ids, Long customerId);
    Optional<CartItem> findByCustomerIdAndProductId(Long customerId, Long productId);
    void deleteByCustomerId(Long customerId);

    default List<CartItem> findByCustomerIdAndOptionalIds(Long customerId, Collection<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            return findByIdInAndCustomerId(ids, customerId);
        }
        return findByCustomerId(customerId);
    }
}
