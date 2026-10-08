package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);

    @Query(value = "SELECT o FROM Order o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH c.user u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status)",
           countQuery = "SELECT COUNT(o) FROM Order o WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:status IS NULL OR o.status = :status)")
    Page<Order> searchOrders(@Param("keyword") String keyword,
                             @Param("status") OrderStatus status,
                             Pageable pageable);

    @Query(value = """
            WITH BaseOrders AS (
                SELECT 
                    o.id,
                    o.status,
                    o.total_amount,
                    CASE WHEN :groupBy = 'DAY' THEN CONVERT(VARCHAR(10), o.created_at, 120) 
                         ELSE CONVERT(VARCHAR(7), o.created_at, 120) 
                    END AS period
                FROM orders o
                WHERE o.created_at >= :startDate AND o.created_at <= :endDate
            )
            SELECT 
                b.period AS period,
                COUNT(b.id) AS totalOrders,
                ISNULL(SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completedOrders,
                ISNULL(SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS confirmedOrders,
                ISNULL(SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelledOrders,
                ISNULL(SUM(CASE WHEN b.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingOrders,
                ISNULL(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.total_amount ELSE 0 END), 0) AS totalRevenue,
                ISNULL(ROUND(CAST(SUM(CASE WHEN b.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) * 100.0 / NULLIF(COUNT(b.id), 0) AS FLOAT), 2), 0.0) AS successRate,
                ISNULL(ROUND(CAST(SUM(CASE WHEN b.status = 'CANCELLED' THEN 1.0 ELSE 0.0 END) * 100.0 / NULLIF(COUNT(b.id), 0) AS FLOAT), 2), 0.0) AS cancellationRate
            FROM BaseOrders b
            GROUP BY b.period
            ORDER BY b.period DESC
            """, nativeQuery = true)
    List<OrderRevenueStatisticProjection> getOrderRevenueStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("groupBy") String groupBy);
}

