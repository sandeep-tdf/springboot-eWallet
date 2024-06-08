package com.app.config;

import com.app.kafkaPayloads.TransactionCompletePayload;
import com.app.kafkaPayloads.TransactionInitPayload;
import com.app.kafkaPayloads.UserCreatePayload;
import com.app.entity.Wallet;
import com.app.exception.InsufficientBalanceException;
import com.app.kafkaPayloads.WalletUpdatedPayload;
import com.app.repository.WalletRepository;
import com.app.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String TRANSACTION_COMP_TOPIC = "TXN-COMPLETE";

    private static final String WALLET_UPDATED_TOPIC = "WALLET-UPDATED";

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private WalletService walletService;

    @Autowired
    private WalletRepository walletRepository;

    @KafkaListener(topics = "USER-CREATED", groupId = "walletApp")
    public void consumeFromUserCreateTopic(ConsumerRecord<String, Object> payload) throws JsonProcessingException {
        UserCreatePayload userCreatePayload = objectMapper.readValue(payload.value().toString(), UserCreatePayload.class);
        MDC.put("requestId",userCreatePayload.getRequestId());
        LOGGER.info("User Create Payload Consumed : {}", payload);
        Wallet wallet = Wallet.builder()
                .userId(userCreatePayload.getUserId())
                .name(userCreatePayload.getUserName())
                .email(userCreatePayload.getEmail())
                .balance(100.00)
                .build();
        MDC.clear();
        walletRepository.save(wallet);
    }

    @KafkaListener(topics = "TXN-INIT", groupId = "walletApp")
    public void consumeFromTxnInitTopic(ConsumerRecord<String, Object> payload) throws JsonProcessingException, ExecutionException, InterruptedException {
        TransactionInitPayload transactionInitPayload = objectMapper.readValue(payload.value().toString(), TransactionInitPayload.class);
        MDC.put("requestId",transactionInitPayload.getRequestId());
        LOGGER.info("Transaction init Payload Consumed : {}", payload);
        TransactionCompletePayload transactionCompletePayload = new TransactionCompletePayload();
        transactionCompletePayload.setId(transactionInitPayload.getId());
        transactionCompletePayload.setRequestId(transactionInitPayload.getRequestId());
        try {
            walletService.doWalletTransaction(transactionInitPayload);
            transactionCompletePayload.setSuccess(Boolean.TRUE);
        } catch (InsufficientBalanceException e) {
            transactionCompletePayload.setSuccess(Boolean.FALSE);
            transactionCompletePayload.setReason(e.getMessage());
        }
        catch (Exception ex) {
            transactionCompletePayload.setSuccess(Boolean.FALSE);
            transactionCompletePayload.setReason("Server Error");
        }

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TRANSACTION_COMP_TOPIC, String.valueOf(transactionInitPayload.getFromId()), transactionCompletePayload);

        LOGGER.info("Pushed Transaction Complete payload to Kafka : {}", future.get());

        MDC.clear();
    }
}
