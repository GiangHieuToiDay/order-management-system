package com.trainning.ordersystem.service;

import com.trainning.ordersystem.messaging.event.order.OrderCreatedEvent;

public interface OrderNotificationService {

    public void sendOrderCreatedEmail(OrderCreatedEvent event);

}
