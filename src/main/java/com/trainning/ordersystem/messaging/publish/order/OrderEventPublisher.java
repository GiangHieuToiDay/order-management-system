//package com.trainning.ordersystem.messaging.publish.order;
//
//import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OrderEventPublisher {
//
//    private final RabbitTemplate rabbitTemplate;
//
//    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
//        this.rabbitTemplate = rabbitTemplate;
//    }
//
//    public void publishOrderCreated(OrderCreatedEvent event) {
//        rabbitTemplate.convertAndSend(
//                "order.exchange",
//                "order.created",
//                event
//        );
//    }
//}
