package com.app.service;

import com.app.kafkaPayloads.TransactionInitPayload;
import com.app.constant.TransactionStatusEnum;
import com.app.dto.TransactionRequest;
import com.app.dto.TxnStatusDTO;
import com.app.entity.TransactionEntity;
import com.app.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class TransactionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);

    private static final String TRANSACTION_INIT_TOPIC = "TXN-INIT";

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    public String doTransaction(TransactionRequest transactionRequest) throws ExecutionException, InterruptedException {

        TransactionEntity transactionEntity = TransactionEntity.builder()
                .fromId(transactionRequest.getFromId())
                .toId(transactionRequest.getToId())
                .amount(transactionRequest.getAmount())
                .remark(transactionRequest.getRemark())
                .status(TransactionStatusEnum.PENDING)
                .txnId(UUID.randomUUID().toString())
                .build();
        transactionRepository.save(transactionEntity);

        TransactionInitPayload transactionInitPayload = TransactionInitPayload.builder()
                .id(transactionEntity.getId())
                .fromId(transactionEntity.getFromId())
                .toId(transactionEntity.getToId())
                .amount(transactionEntity.getAmount())
                .remark(transactionEntity.getRemark())
                .requestId(MDC.get("requestId"))
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(TRANSACTION_INIT_TOPIC, String.valueOf(transactionEntity.getFromId()), transactionInitPayload);

        LOGGER.info("Pushed Transaction init payload to Kafka : {}", future.get());

        return transactionEntity.getTxnId();
    }

    public TxnStatusDTO getStatus(String txnId) {

        TransactionEntity transaction = transactionRepository.findByTxnId(txnId);
        if(transaction == null) {
            return null;
        }
        return TxnStatusDTO.builder()
                .status(String.valueOf(transaction.getStatus()))
                .reason(transaction.getReason())
                .build();
    }
}
