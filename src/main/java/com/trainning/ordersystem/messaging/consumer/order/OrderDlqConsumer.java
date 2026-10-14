package com.trainning.ordersystem.messaging.consumer.order;

import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderDlqConsumer {

    @RabbitListener(queues = RabbitMQConfig.DLQ_QUEUE)
    public void processFailedOrder(OrderCreatedEvent event) {
        log.warn("[DLQ ALERT] Phát hiện đơn hàng xử lý thất bại chuyển vào Dead Letter Queue: orderId={}, orderCode={}",
                event.getOrderId(), event.getOrderCode());

        // mở rộng tại đây:
        // - Lưu vết vào DB
        // - Gửi thông báo khẩn cấp
    }
}
