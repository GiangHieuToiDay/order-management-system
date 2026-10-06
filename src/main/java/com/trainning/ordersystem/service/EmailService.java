package com.trainning.ordersystem.service;

public interface EmailService {

    public void sendEmail(
            String to,
            String subject,
            String content
    );

}
