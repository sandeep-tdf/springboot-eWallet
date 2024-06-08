package com.app.controller;

import com.app.dto.TransactionRequest;
import com.app.dto.TxnStatusDTO;
import com.app.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/transaction-service")
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/doTransaction")
    ResponseEntity<String> doTransaction(@RequestBody TransactionRequest request) throws ExecutionException, InterruptedException {
        LOGGER.info("Initiating txn : {} ", request);
        return ResponseEntity.ok(transactionService.doTransaction(request));
    }

    @GetMapping("/status/{txnId}")
    ResponseEntity<TxnStatusDTO> getStatus(@PathVariable("txnId") String txnId) {
        LOGGER.info("Fetching txn status for txnId {}", txnId);
        return ResponseEntity.ok(transactionService.getStatus(txnId));
    }

}
