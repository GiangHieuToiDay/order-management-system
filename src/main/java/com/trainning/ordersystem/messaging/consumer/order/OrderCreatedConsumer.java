package com.trainning.ordersystem.messaging.consumer.order;

import com.rabbitmq.client.Channel;
import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import com.trainning.ordersystem.service.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedConsumer {

    private final OrderNotificationService notificationService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeOrder(
            OrderCreatedEvent event,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {

        try {
            log.info("Nhận OrderCreatedEvent: orderId={}", event.getOrderId());

            notificationService.sendOrderCreatedEmail(event);

            channel.basicAck(deliveryTag, false);

            log.info("OrderCreatedEvent xử lý thành công: orderId={}",
                    event.getOrderId());

        } catch (IllegalArgumentException ex) {

            log.error("Lỗi nghiệp vụ: orderId={}, message={}",
                    event.getOrderId(), ex.getMessage());

            channel.basicNack(deliveryTag, false, false);

        } catch (Exception ex) {

            log.error("Lỗi tạm khi xử lý orderId={}",
                    event.getOrderId(), ex);

            channel.basicNack(deliveryTag, false, true);
        }
    }
}
