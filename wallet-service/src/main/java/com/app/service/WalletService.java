package com.app.service;

import com.app.kafkaPayloads.TransactionInitPayload;
import com.app.entity.Wallet;
import com.app.exception.InsufficientBalanceException;
import com.app.kafkaPayloads.WalletUpdatedPayload;
import com.app.repository.WalletRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class WalletService {

    public static final Logger LOGGER = LoggerFactory.getLogger(WalletService.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private static final String WALLET_UPDATED_TOPIC = "WALLET-UPDATED";

    @Transactional
    public void doWalletTransaction(TransactionInitPayload transactionInitPayload) throws InsufficientBalanceException, ExecutionException, InterruptedException {

        Wallet fromWallet = walletRepository.findByUserId(transactionInitPayload.getFromId());

        if (fromWallet.getBalance() >= transactionInitPayload.getAmount()) {
            Wallet toWallet = walletRepository.findByUserId(transactionInitPayload.getToId());
            toWallet.setBalance(toWallet.getBalance() + transactionInitPayload.getAmount());
            fromWallet.setBalance(fromWallet.getBalance() - transactionInitPayload.getAmount());
            walletRepository.save(fromWallet);
            walletRepository.save(toWallet);

            WalletUpdatedPayload walletUpdatedPayloadFrom = WalletUpdatedPayload.builder()
                    .userEmail(fromWallet.getEmail())
                    .userName(fromWallet.getName())
                    .balance(fromWallet.getBalance())
                    .requestId(transactionInitPayload.getRequestId())
                    .build();

            CompletableFuture<SendResult<String, Object>> futureFrom =
                    kafkaTemplate.send(WALLET_UPDATED_TOPIC, String.valueOf(transactionInitPayload.getFromId()), walletUpdatedPayloadFrom);

            LOGGER.info("Pushed walletUpdatedPayloadFrom to Kafka : {}", futureFrom.get());

            WalletUpdatedPayload walletUpdatedPayloadTo = WalletUpdatedPayload.builder()
                    .userEmail(toWallet.getEmail())
                    .userName(toWallet.getName())
                    .balance(toWallet.getBalance())
                    .requestId(transactionInitPayload.getRequestId())
                    .build();

            CompletableFuture<SendResult<String, Object>> futureTo =
                    kafkaTemplate.send(WALLET_UPDATED_TOPIC, String.valueOf(transactionInitPayload.getToId()), walletUpdatedPayloadTo);

            LOGGER.info("Pushed walletUpdatedPayloadTo to Kafka : {}", futureTo.get());
        }
        else {
            throw new InsufficientBalanceException("Low Balance");
        }
    }
}
