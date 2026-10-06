package com.trainning.ordersystem.controller;

import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.service.OrderService;
import com.trainning.ordersystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "true") boolean isAdminOrStaff) throws Exception {

        log.info("Yêu cầu xuất hóa đơn PDF cho đơn hàng ID={}", orderId);
        OrderDetailResponse order = orderService.getOrderById(orderId, customerId, isAdminOrStaff);
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
            @RequestParam(defaultValue = "1") Long customerId,
            @RequestParam(defaultValue = "true") boolean isAdminOrStaff) throws Exception {

        log.info("Yêu cầu xuất hóa đơn PDF cho đơn hàng mã={}", orderCode);
        OrderDetailResponse order = orderService.getOrderByCode(orderCode, customerId, isAdminOrStaff);
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
}
