package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import java.util.stream.StreamSupport;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class TransactionProcessor {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionProcessor(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public void processTransaction(Transaction transaction) {
        UserRecord sender = userRepository.findById(transaction.getSenderId()).orElse(null);
        UserRecord recipient = userRepository.findById(transaction.getRecipientId()).orElse(null);

        if (sender == null || recipient == null) {
            return;
        }

        if (sender.getBalance() < transaction.getAmount()) {
            return;
        }

        sender.setBalance(sender.getBalance() - transaction.getAmount());
        recipient.setBalance(recipient.getBalance() + transaction.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);

        TransactionRecord record = new TransactionRecord(sender, recipient, transaction.getAmount());
        transactionRepository.save(record);
        

        var waldorf = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .filter(u -> u.getName().equals("waldorf"))
                .findFirst()
                .orElse(null);

        if (waldorf != null) {
            System.out.println("waldorf balance = " + waldorf.getBalance());
        }

    }
}
