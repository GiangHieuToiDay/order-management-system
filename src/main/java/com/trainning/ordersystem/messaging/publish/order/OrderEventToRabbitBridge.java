package com.trainning.ordersystem.messaging.publish.order;

import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderEventToRabbitBridge {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleInternalOrderEvent(OrderCreatedEvent event) {
        System.out.println("Tự động bắt được event nội bộ, chuyển tiếp lên RabbitMQ...");

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                event
        );
    }
}
