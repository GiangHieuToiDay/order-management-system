package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.common.ApiResponse;
import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.dto.response.report.OrderRevenueStatisticResponse;
import com.trainning.ordersystem.security.SecurityUtils;
import com.trainning.ordersystem.service.OrderService;
import com.trainning.ordersystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final OrderService orderService;

    @GetMapping("/orders/{orderId}/bill")
    public ResponseEntity<byte[]> exportOrderBill(
            @PathVariable Long orderId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Boolean isAdminOrStaff) throws Exception {

        boolean adminOrStaff = SecurityUtils.isAdminOrStaff();
        Long effectiveCustomerId = adminOrStaff && customerId != null ? customerId : SecurityUtils.getCurrentCustomerId();

        log.info("Yêu cầu xuất hóa đơn PDF cho đơn hàng ID={}, customerId={}, isAdminOrStaff={}",
                orderId, effectiveCustomerId, adminOrStaff);
        OrderDetailResponse order = orderService.getOrderById(orderId, effectiveCustomerId, adminOrStaff);
        byte[] pdfBytes = reportService.generateBill(order);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("bill-" + order.getOrderCode() + ".pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/orders/code/{orderCode}/bill")
    public ResponseEntity<byte[]> exportOrderBillByCode(
            @PathVariable String orderCode,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Boolean isAdminOrStaff) throws Exception {

        boolean adminOrStaff = SecurityUtils.isAdminOrStaff();
        Long effectiveCustomerId = adminOrStaff && customerId != null ? customerId : SecurityUtils.getCurrentCustomerId();

        log.info("Yêu cầu xuất hóa đơn PDF cho đơn hàng mã={}, customerId={}, isAdminOrStaff={}",
                orderCode, effectiveCustomerId, adminOrStaff);
        OrderDetailResponse order = orderService.getOrderByCode(orderCode, effectiveCustomerId, adminOrStaff);
        byte[] pdfBytes = reportService.generateBill(order);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline()
                .filename("bill-" + order.getOrderCode() + ".pdf")
                .build());
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<List<OrderRevenueStatisticResponse>>> getRevenueStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "MONTH") String groupBy) {

        log.info("Lấy báo cáo doanh thu: startDate={}, endDate={}, groupBy={}", startDate, endDate, groupBy);
        List<OrderRevenueStatisticResponse> response = orderService.getRevenueStatistics(startDate, endDate, groupBy);
        return ResponseEntity.ok(ApiResponse.ok("Lấy báo cáo doanh thu và tỷ lệ đơn hàng thành công", response));
    }
}
