package com.trainning.ordersystem.messaging.consumer.order;

import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
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
        String traceId = event.getOrderCode() != null ? "mq-" + event.getOrderCode() : "mq-" + event.getOrderId();
        MDC.put("traceId", traceId);

        try {
            log.info("[RABBITMQ][CONSUME] Bắt đầu xử lý OrderCreatedEvent | orderId={}, orderCode={}", event.getOrderId(), event.getOrderCode());

            // 1. Gửi email thông báo (thành công Spring sẽ tự động ACK)
            notificationService.sendOrderCreatedEmail(event);
            log.info("[RABBITMQ][SUCCESS] Xử lý OrderCreatedEvent thành công | orderId={}", event.getOrderId());

        } catch (IllegalArgumentException ex) {
            log.error("[RABBITMQ][REJECT_BUSINESS_ERROR] Lỗi nghiệp vụ không thể thử lại | orderId={}, message={}", event.getOrderId(), ex.getMessage());
            throw new AmqpRejectAndDontRequeueException(ex);

        } catch (Exception ex) {
            log.error("[RABBITMQ][RETRY_TRANSIENT_ERROR] Lỗi xử lý đơn hàng, chuẩn bị retry | orderId={}, message={}", event.getOrderId(), ex.getMessage(), ex);
            throw new RuntimeException(ex);
        } finally {
            MDC.remove("traceId");
        }
    }
}
