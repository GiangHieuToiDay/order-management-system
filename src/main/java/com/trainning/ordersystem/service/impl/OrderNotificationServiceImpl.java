package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.dto.response.order.OrderDetailResponse;
import com.trainning.ordersystem.entity.Order;
import com.trainning.ordersystem.exception.AppException;
import com.trainning.ordersystem.exception.ErrorCode;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.service.EmailService;
import com.trainning.ordersystem.service.OrderNotificationService;
import com.trainning.ordersystem.service.OrderService;
import com.trainning.ordersystem.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderNotificationServiceImpl implements OrderNotificationService {

    private final EmailService emailService;
    private final ReportService reportService;
    private final OrderService orderService;




    public void sendOrderCreatedEmail(OrderCreatedEvent event) {

        OrderDetailResponse order = orderService.getOrderById(event.getOrderId(), event.getCustomerId(), true);

        byte[] pdf = null;

        try{
            pdf = reportService.generateBill(order);
        }catch (Exception e){
            throw new AppException(ErrorCode.CANNOT_EXPORT_FILE);
        }

        emailService.sendEmailWithAttachment(
                "ganrt.hieu.08@gmail.com",
                "Xác nhận đơn hàng " + order.getOrderCode(),
                "Cảm ơn bạn đã đặt hàng. Hóa đơn được đính kèm trong email.",
                pdf,
                "bill-" + order.getOrderCode() + ".pdf"
        );


    }
}
