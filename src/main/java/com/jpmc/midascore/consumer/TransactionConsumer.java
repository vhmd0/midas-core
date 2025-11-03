package com.jpmc.midascore.consumer;

import com.jpmc.midascore.foundation.Transaction;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionConsumer {

    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-group")
    public void consume(Transaction transaction) {
        System.out.println("Received transaction: " + transaction);
    }
}
