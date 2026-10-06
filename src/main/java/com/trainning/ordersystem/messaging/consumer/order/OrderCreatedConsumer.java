//package com.trainning.ordersystem.messaging.consumer.order;
//
//import com.trainning.ordersystem.config.rabbit.RabbitMQConfig;
//import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;
//import com.trainning.ordersystem.service.OrderItemService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class OrderCreatedConsumer {
//
//    private final OrderItemService orderItemService;
//
//    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
//    public void handleOrderCreated(OrderCreatedEvent event) {
//        log.info("Nhận sự kiện OrderCreatedEvent cho đơn hàng: orderCode={}", event.getOrderCode());
//
////        orderItemService.deductStock(
////                event.getOrderCode(),
////                event.getItems()
////        );
//    }
//}
