package com.trainning.ordersystem.repository;

import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.entity.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH c.user WHERE o.orderCode = :orderCode")
    Optional<Order> findByOrderCode(@Param("orderCode") String orderCode);

    @Override
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer c LEFT JOIN FETCH c.user WHERE o.id = :id")
    Optional<Order> findById(@Param("id") Long id);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @NativeQuery(
            value = """
                        SELECT o.id AS id,
                               o.order_code AS orderCode,
                               c.id AS customerId,
                               u.full_name AS customerName,
                               u.email AS customerEmail,
                               u.phone AS customerPhone,
                               u.full_name AS recipientName,
                               u.phone AS recipientPhone,
                               o.shipping_address AS shippingAddress,
                               CAST(NULL AS VARCHAR(255)) AS note,
                               o.total_amount AS totalAmount,
                               o.status AS status,
                               o.payment_method AS paymentMethod,
                               o.created_at AS createdAt,
                               o.updated_at AS updatedAt
                        FROM orders o
                        LEFT JOIN customers c ON o.customer_id = c.id
                        LEFT JOIN users u ON c.user_id = u.id
                        WHERE o.order_code = :orderCode
                    """,
            sqlResultSetMapping = "OrderDetailResponseMapping"
    )
    Optional<OrderDetailResponse> findDetailResponseByCode(@Param("orderCode") String orderCode);

    @NativeQuery(
            value = """
                        SELECT o.id AS id,
                               o.order_code AS orderCode,
                               c.id AS customerId,
                               u.full_name AS customerName,
                               u.full_name AS recipientName,
                               o.total_amount AS totalAmount,
                               o.status AS status,
                               o.payment_method AS paymentMethod,
                               (SELECT COUNT(oi.id) FROM order_items oi WHERE oi.order_id = o.id) AS totalItems,
                               o.created_at AS createdAt
                        FROM orders o
                        LEFT JOIN customers c ON o.customer_id = c.id
                        LEFT JOIN users u ON c.user_id = u.id
                        WHERE c.id = :customerId
                    """,
            countQuery = """
                        SELECT COUNT(o.id)
                        FROM orders o
                        WHERE o.customer_id = :customerId
                    """,
            sqlResultSetMapping = "OrderSummaryResponseMapping"
    )
    Page<OrderSummaryResponse> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @NativeQuery(
            value = """
                        SELECT o.id AS id,
                               o.order_code AS orderCode,
                               c.id AS customerId,
                               u.full_name AS customerName,
                               u.full_name AS recipientName,
                               o.total_amount AS totalAmount,
                               o.status AS status,
                               o.payment_method AS paymentMethod,
                               (SELECT COUNT(oi.id) FROM order_items oi WHERE oi.order_id = o.id) AS totalItems,
                               o.created_at AS createdAt
                        FROM orders o
                        LEFT JOIN customers c ON o.customer_id = c.id
                        LEFT JOIN users u ON c.user_id = u.id
                        WHERE (:keyword IS NULL OR :keyword = '' 
                               OR LOWER(o.order_code) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                               OR LOWER(o.shipping_address) LIKE LOWER(CONCAT('%', :keyword, '%')))
                          AND (:status IS NULL OR o.status = :status)
                    """,
            countQuery = """
                        SELECT COUNT(o.id)
                        FROM orders o
                        WHERE (:keyword IS NULL OR :keyword = '' 
                               OR LOWER(o.order_code) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                               OR LOWER(o.shipping_address) LIKE LOWER(CONCAT('%', :keyword, '%')))
                          AND (:status IS NULL OR o.status = :status)
                    """,
            sqlResultSetMapping = "OrderSummaryResponseMapping"
    )
    Page<OrderSummaryResponse> searchOrders(
            @Param("keyword") String keyword,
            @Param("status") String status,
            Pageable pageable
    );


    // querry hết dữ liệu order theo điều kiện và dùng bảng tạm để lưu
    // group by luôn chạy trc
    // từ cái base => group theo 2 nhóm chính: period: tổng hợp và period 2 gồm id các thứ=> dòng con chứ detail
    // tính toán xong thì order by customer id => lúc ny dòng cha null sẽ đẩy lên đầu
    @NativeQuery(
            value = """
                    WITH BaseOrders AS (
                        SELECT 
                            o.id,
                            o.status,
                            o.total_amount,
                            c.id AS customerId,
                            u.full_name AS customerName,
                            u.phone AS customerPhone,
                            u.email AS customerEmail,
                            CASE WHEN :groupBy = 'DAY' THEN CONVERT(VARCHAR(10), o.created_at, 120) 
                                 ELSE CONVERT(VARCHAR(7), o.created_at, 120) 
                            END AS period
                        FROM orders o
                        LEFT JOIN customers c ON o.customer_id = c.id
                        LEFT JOIN users u ON c.user_id = u.id
                        WHERE o.created_at >= :startDate AND o.created_at <= :endDate
                    )
                    SELECT
                        b.period AS period,
                        b.customerId AS customerId,
                        b.customerName AS customerName,
                        b.customerPhone AS customerPhone,
                        b.customerEmail AS customerEmail,
                        COUNT(b.id) AS totalOrders,
                        ISNULL(SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END), 0) AS completedOrders,
                        ISNULL(SUM(CASE WHEN b.status = 'CONFIRMED' THEN 1 ELSE 0 END), 0) AS confirmedOrders,
                        ISNULL(SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END), 0) AS cancelledOrders,
                        ISNULL(SUM(CASE WHEN b.status = 'PENDING' THEN 1 ELSE 0 END), 0) AS pendingOrders,
                        ISNULL(SUM(CASE WHEN b.status = 'COMPLETED' THEN b.total_amount ELSE 0 END), 0) AS totalRevenue,
                        ISNULL(ROUND(CAST(SUM(CASE WHEN b.status = 'COMPLETED' THEN 1.0 ELSE 0.0 END) * 100.0 / NULLIF(COUNT(b.id), 0) AS FLOAT), 2), 0.0) AS successRate,
                        ISNULL(ROUND(CAST(SUM(CASE WHEN b.status = 'CANCELLED' THEN 1.0 ELSE 0.0 END) * 100.0 / NULLIF(COUNT(b.id), 0) AS FLOAT), 2), 0.0) AS cancellationRate
                    FROM BaseOrders b
                    GROUP BY GROUPING SETS (
                        (b.period),
                        (b.period, b.customerId, b.customerName, b.customerPhone, b.customerEmail)
                    )
                    ORDER BY 
                        b.period DESC, 
                        CASE WHEN b.customerId IS NULL THEN 0 ELSE 1 END ASC,
                        totalRevenue DESC
                    """,
            sqlResultSetMapping = "OrderRevenueStatisticMapping"
    )
    List<OrderRevenueStatisticResponse> getOrderRevenueStatistics(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("groupBy") String groupBy
    );


}
