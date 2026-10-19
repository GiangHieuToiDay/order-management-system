package com.trainning.ordersystem.dto.response.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Tự động ẩn các trường null khi trả về JSON
public class OrderRevenueStatisticResponse {

    private String period;

    // Nếu là dòng Tổng của Kỳ thì 4 trường này sẽ NULL -> Jackson tự ẩn khi lên JSON
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Bộ chỉ số thống kê dùng chung
    private Long totalOrders;
    private Long completedOrders;
    private Long confirmedOrders;
    private Long cancelledOrders;
    private Long pendingOrders;
    private BigDecimal totalRevenue;
    private Double successRate;
    private Double cancellationRate;

    private List<OrderRevenueStatisticResponse> customerDetails;

    public OrderRevenueStatisticResponse(
            String period,
            Long customerId,
            String customerName,
            String customerPhone,
            String customerEmail,
            Long totalOrders,
            Long completedOrders,
            Long confirmedOrders,
            Long cancelledOrders,
            Long pendingOrders,
            BigDecimal totalRevenue,
            Double successRate,
            Double cancellationRate
    ) {
        this.period = period;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.totalOrders = totalOrders;
        this.completedOrders = completedOrders;
        this.confirmedOrders = confirmedOrders;
        this.cancelledOrders = cancelledOrders;
        this.pendingOrders = pendingOrders;
        this.totalRevenue = totalRevenue;
        this.successRate = successRate;
        this.cancellationRate = cancellationRate;
    }
}

