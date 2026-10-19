package com.trainning.ordersystem.repository.impl;

import com.trainning.ordersystem.dto.response.order.OrderSummaryResponse;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.repository.OrderRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<OrderSummaryResponse> findByCustomerId(Long customerId, Pageable pageable) {
        String sql = """
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
            ORDER BY o.created_at DESC
        """;

        String countSql = """
            SELECT COUNT(o.id)
            FROM orders o
            WHERE o.customer_id = :customerId
        """;

        Query query = entityManager.createNativeQuery(sql, "OrderSummaryResponseMapping");
        query.setParameter("customerId", customerId);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<OrderSummaryResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("customerId", customerId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<OrderSummaryResponse> searchOrders(String keyword, String status, Pageable pageable) {
        String sql = """
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
              AND (:status IS NULL OR :status = '' OR o.status = :status)
            ORDER BY o.created_at DESC
        """;

        String countSql = """
            SELECT COUNT(o.id)
            FROM orders o
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(o.order_code) LIKE LOWER(CONCAT('%', :keyword, '%')) 
                   OR LOWER(o.shipping_address) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:status IS NULL OR :status = '' OR o.status = :status)
        """;

        Query query = entityManager.createNativeQuery(sql, "OrderSummaryResponseMapping");
        query.setParameter("keyword", keyword);
        query.setParameter("status", status);

        if (pageable != null && pageable.isPaged()) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }

        @SuppressWarnings("unchecked")
        List<OrderSummaryResponse> content = query.getResultList();

        Query countQuery = entityManager.createNativeQuery(countSql);
        countQuery.setParameter("keyword", keyword);
        countQuery.setParameter("status", status);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<OrderRevenueStatisticResponse> getOrderRevenueStatistics(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String groupBy
    ) {
        String sql = """
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
        """;

        Query query = entityManager.createNativeQuery(sql, "OrderRevenueStatisticMapping");
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("groupBy", groupBy);

        @SuppressWarnings("unchecked")
        List<OrderRevenueStatisticResponse> results = query.getResultList();
        return results;
    }
}