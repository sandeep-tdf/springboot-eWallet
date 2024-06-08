package com.app.config;

import com.app.kafkaPayloads.TransactionCompletePayload;
import com.app.constant.TransactionStatusEnum;
import com.app.entity.TransactionEntity;
import com.app.repository.TransactionRepository;
import com.app.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class KafkaTransactionCompleteConsumerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionCompleteConsumerConfig.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionService transactionService;

    @KafkaListener(topics = "TXN-COMPLETE", groupId = "transactionApp")
    public void transactionCompletePayloadConsumer(ConsumerRecord<String, Object> payload) throws JsonProcessingException {
        TransactionCompletePayload transactionCompletePayload = objectMapper.readValue(payload.value().toString(), TransactionCompletePayload.class);
        MDC.put("requestId",transactionCompletePayload.getRequestId());
        LOGGER.info("Transaction Complete Payload Consumed : {}", payload);
        TransactionEntity transactionEntity = transactionRepository.findById(transactionCompletePayload.getId()).orElse(null);
        if (transactionEntity != null) {
            if(transactionCompletePayload.getSuccess()){
                transactionEntity.setStatus(TransactionStatusEnum.SUCCESS);
            }
            else {
                transactionEntity.setStatus(TransactionStatusEnum.FAILED);
                transactionEntity.setReason(transactionCompletePayload.getReason());
            }
            transactionRepository.save(transactionEntity);
        }
        MDC.clear();
    }
}
