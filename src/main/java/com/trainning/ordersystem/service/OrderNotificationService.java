package com.trainning.ordersystem.service;

import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;

public interface OrderNotificationService {

     void sendOrderCreatedEmail(OrderCreatedEvent event);

}
