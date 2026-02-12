package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Transaction;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.TransactionService;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor // Koristi ovo da ne pišeš konstruktor ručno
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addTokens(@RequestBody Map<String, Object> payload, java.security.Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(401).body("Niste ulogovani");

            // 1. Identitet uzimamo iz TOKENA, ne iz JSON-a
            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            // 2. Podaci iz Paypala
            Integer amount = (Integer) payload.get("amount");
            String transactionId = (String) payload.get("transactionId");

            // ZAŠTITA: Provjeri da li je neko već iskoristio ovaj isti PayPal ID
            // (Moraš dodati metodu existsByPaypalOrderId u TransactionRepository)
            if (transactionService.isTransactionProcessed(transactionId)) {
                return ResponseEntity.badRequest().body("Ova transakcija je već obrađena!");
            }

            if (amount == null || amount <= 0) {
                return ResponseEntity.badRequest().body("Nevalidan iznos");
            }

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(amount);
            transaction.setPaypalOrderId(transactionId);

            Transaction saved = transactionService.createTransaction(transaction);

            return ResponseEntity.ok(saved.getUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška: " + e.getMessage());
        }
    }
}