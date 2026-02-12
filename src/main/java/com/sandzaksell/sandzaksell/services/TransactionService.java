package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Transaction;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.TransactionRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionService(TransactionRepository transactionRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }

    public boolean isTransactionProcessed(String paypalId) {
        return transactionRepository.existsByPaypalOrderId(paypalId);
    }


    @Transactional
    public Transaction createTransaction(Transaction transaction) {
        // 1. Spasimo transakciju u bazu
        Transaction savedTransaction = transactionRepository.save(transaction);

        // 2. Uzimamo korisnika i dodajemo tokene na postojeći balans
        User user = savedTransaction.getUser();
        int currentBalance = user.getTokenBalance() != null ? user.getTokenBalance() : 0;
        user.setTokenBalance(currentBalance + savedTransaction.getAmount());

        userRepository.save(user);

        return savedTransaction;
    }
}