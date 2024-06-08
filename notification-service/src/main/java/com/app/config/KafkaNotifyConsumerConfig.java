package com.app.config;

import com.app.kafkaPayloads.UserCreatePayload;
import com.app.kafkaPayloads.WalletUpdatedPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class KafkaNotifyConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaNotifyConsumerConfig.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JavaMailSender javaMailSender;

    @KafkaListener(topics = "USER-CREATED", groupId = "notificationApp")
    public void consumerUserCreatePayload(ConsumerRecord<String, Object> payload) throws JsonProcessingException {

        UserCreatePayload userCreatePayload = objectMapper.readValue(payload.value().toString(), UserCreatePayload.class);
        MDC.put("requestId", userCreatePayload.getRequestId());
        LOGGER.info("User Create Payload Consumed : {}", payload);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("tdfsandeep@gmail.com");
        simpleMailMessage.setSubject("Welcome " + userCreatePayload.getUserName() + " !");
        simpleMailMessage.setTo(userCreatePayload.getEmail());
        simpleMailMessage.setText("Hi " + userCreatePayload.getUserId() + ", welcome to my world.");
        simpleMailMessage.setCc("admin.wallet@yopmail.com");
        javaMailSender.send(simpleMailMessage);
        MDC.clear();

    }

    @KafkaListener(topics = "WALLET-UPDATED", groupId = "notificationApp")
    public void consumerWalletUpdatedPayload(ConsumerRecord<String, Object> payload) throws JsonProcessingException {

        WalletUpdatedPayload walletUpdatedPayload = objectMapper.readValue(payload.value().toString(), WalletUpdatedPayload.class);
        MDC.put("requestId", walletUpdatedPayload.getRequestId());
        LOGGER.info("User Create Payload Consumed : {}", payload);

        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("tdfsandeep@gmail.com");
        simpleMailMessage.setSubject(walletUpdatedPayload.getUserName() + "'s wallet updated.");
        simpleMailMessage.setTo(walletUpdatedPayload.getUserEmail());
        simpleMailMessage.setText("Hi " + walletUpdatedPayload.getUserName() + ", Your wallet updated new balance is "+ walletUpdatedPayload.getBalance());
        simpleMailMessage.setCc("admin.wallet@yopmail.com");
        javaMailSender.send(simpleMailMessage);
        MDC.clear();

    }
}
