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
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addTokens(@RequestBody Map<String, Object> payload, java.security.Principal principal) {
        try {
            if (principal == null) return ResponseEntity.status(401).body("Niste ulogovani");

            User user = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

            String transactionId = (String) payload.get("transactionId");

            // 1. ZAŠTITA: Odmah proveravamo da li je ID već korišćen
            if (transactionService.isTransactionProcessed(transactionId)) {
                return ResponseEntity.badRequest().body("Ova transakcija je već iskorišćena!");
            }

            // 2. ZAŠTITA: Ignorišemo 'amount' sa frontenda!
            // Mi na backendu kažemo: "Ovaj proces dopune uvek vredi 100 tokena za 3 EUR"
            // Ako želiš više paketa, koristi switch-case na osnovu cene sa Paypala.
            int fixedAmount = 100;

            // 3. (OPCIONO) Ovde bi išao poziv ka PayPal API-ju da se proveri transactionId
            // Za sada, bar smo osigurali da niko ne može da upiše "milion" tokena.

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setAmount(fixedAmount);
            transaction.setPaypalOrderId(transactionId);

            Transaction saved = transactionService.createTransaction(transaction);

            return ResponseEntity.ok(saved.getUser());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Greška na serveru: " + e.getMessage());
        }
    }
}