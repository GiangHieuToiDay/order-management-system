package com.trainning.ordersystem.messaging.consumer.order;

import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final OrderNotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeOrder(OrderCreatedEvent event) {
        log.info("Nhận OrderCreatedEvent: orderId={}", event.getOrderId());

        try {
            // 1. Gửi email thông báo (thành công Spring sẽ tự động ACK)
            notificationService.sendOrderCreatedEmail(event);
            log.info("OrderCreatedEvent xử lý thành công: orderId={}", event.getOrderId());

        } catch (IllegalArgumentException ex) {
            log.error("Lỗi nghiệp vụ không thể thử lại: orderId={}, message={}", event.getOrderId(), ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);

        } catch (Exception ex) {
            log.error("Lỗi xử lý đơn hàng: orderId={}, message={}", event.getOrderId(), ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
    }
}
