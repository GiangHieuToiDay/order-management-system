package com.trainning.ordersystem.service;

public interface EmailService {

    void sendEmail(
            String to,
            String subject,
            String content
    );

    void sendEmailWithAttachment(
            String to,
            String subject,
            String content,
            byte[] attachment,
            String fileName
    );

}
