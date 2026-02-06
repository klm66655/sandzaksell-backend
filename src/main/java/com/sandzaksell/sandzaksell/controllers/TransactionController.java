package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Transaction;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.TransactionService;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@CrossOrigin(origins = "*") // Dozvoljava React-u da pristupi API-ju
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addTokens(@RequestBody Map<String, Object> payload) {
        try {
            // Podaci koje šalje tvoj React (handlePaymentSuccess)
            Long userId = Long.valueOf(payload.get("userId").toString());
            Integer amount = (Integer) payload.get("amount");
            String transactionId = (String) payload.get("transactionId");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setPaypalOrderId(transactionId);

            Transaction saved = transactionService.createTransaction(transaction);

            // Vraćamo korisnika sa novim balansom nazad u React
            return ResponseEntity.ok(saved.getUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška pri obradi uplate: " + e.getMessage());
        }
    }
}