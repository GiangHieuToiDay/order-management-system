package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticProjection;
import com.trainning.ordersystem.dto.response.report.PeriodOrderDetailResponse;
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
//    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    long countByStatus(OrderStatus status);

    @Query("""
    SELECT new com.trainning.ordersystem.dto.response.order.OrderDetailResponse(
        o.id,o.orderCode,c.id, u.fullName,u.email,u.phone,u.fullName,u.phone,o.shippingAddress,null,o.totalAmount,o.status,o.paymentMethod,o.createdAt,o.updatedAt
    )
    FROM Order o
    LEFT JOIN o.customer c
    LEFT JOIN c.user u
    WHERE o.orderCode = :orderCode
""")
    Optional<OrderDetailResponse> findDetailResponseByCode(@Param("orderCode") String orderCode);

    @Query(
            value = """
            SELECT new com.trainning.ordersystem.dto.response.order.OrderSummaryResponse(
                o.id,o.orderCode,c.id,u.fullName,u.fullName,o.totalAmount, o.status,o.paymentMethod,SIZE(o.orderItems),o.createdAt
            )
            FROM Order o
            LEFT JOIN o.customer c
            LEFT JOIN c.user u
            WHERE c.id = :customerId
        """,
            countQuery = """
            SELECT COUNT(o)
            FROM Order o
            WHERE o.customer.id = :customerId
        """
    )
    Page<OrderSummaryResponse> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);


    @Query(
            value = """
            SELECT new com.trainning.ordersystem.dto.response.order.OrderSummaryResponse(
                o.id,o.orderCode,c.id,u.fullName,u.fullName,o.totalAmount,o.status,o.paymentMethod,SIZE(o.orderItems),o.createdAt
            )
            FROM Order o
            LEFT JOIN o.customer c
            LEFT JOIN c.user u
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR o.status = :status)
        """,
            countQuery = """
            SELECT COUNT(o)
            FROM Order o
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(o.shippingAddress) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR o.status = :status)
        """
    )
    Page<OrderSummaryResponse> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") OrderStatus status,
            Pageable pageable
    );


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
    // dùng isnull do có thể k có đơn nào có status như trên có thể dẫn đến null dùng isnull để thay thế cái null đấy sẽ bằng 0
    // cast để cast sang dạng float
    // sum là tính tổng trng thái complete với canceled rồi * 100 / count tổng, dùng null if để tránh chia cho 0.

    @Query("""
        SELECT new com.trainning.ordersystem.dto.response.report.PeriodOrderDetailResponse(
            o.id,o.orderCode,c.id,u.fullName,u.phone,u.email,o.totalAmount,o.status,o.paymentMethod,o.createdAt
        )
        FROM Order o
        LEFT JOIN o.customer c
        LEFT JOIN c.user u
        WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate
          AND o.status IN :statuses
        ORDER BY o.createdAt DESC
    """)
    List<PeriodOrderDetailResponse> findPeriodOrderDetails(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<OrderStatus> statuses
    );

}

