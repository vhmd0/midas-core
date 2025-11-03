package com.jpmc.midascore.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionListener {

    private final TransactionProcessor transactionProcessor;
    private final ObjectMapper objectMapper;

    public TransactionListener(TransactionProcessor transactionProcessor, ObjectMapper objectMapper) {
        this.transactionProcessor = transactionProcessor;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    public void listen(String message) {
        try {
            Transaction transaction = objectMapper.readValue(message, Transaction.class);
            transactionProcessor.processTransaction(transaction);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
