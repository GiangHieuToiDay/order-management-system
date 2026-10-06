package com.trainning.ordersystem.service.impl;

import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.service.EmailService;
import com.trainning.ordersystem.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class OrderNotificationServiceImpl implements OrderNotificationService {

    private final EmailService emailService;

    public void sendOrderCreatedEmail(OrderCreatedEvent event) {

        String content = buildOrderCreatedContent(event);

        emailService.sendEmail(
                "ganrt.hieu.08@gmail.com",
                "Thông báo đặt hàng",
                content
        );
    }

    private String buildOrderCreatedContent(OrderCreatedEvent event) {

        return "🎉 Quý khách đã đặt đơn hàng #"
                + event.getOrderId()
                + " thành công!\n\n"
                + "Cảm ơn Quý khách đã tin tưởng và lựa chọn dịch vụ của chúng tôi.\n"
                + "Đơn hàng của Quý khách đã được ghi nhận và đang được xử lý.\n\n"
                + "Trân trọng,\n"
                + "Order Management System";
    }
}
