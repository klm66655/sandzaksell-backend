package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Report;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.ReportRepository;
import com.sandzaksell.sandzaksell.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor // Koristi konstruktor umesto @Autowired, profesionalnije je
public class ReportController {

    private final ReportRepository reportRepository;
    private final UserService userService; // Treba nam da nađemo trenutnog korisnika

    @PostMapping("/send")
    public ResponseEntity<?> sendReport(@RequestBody Report report, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Morate biti ulogovani da biste prijavili oglas.");
        }

        // 1. Identitet: Uzimamo korisnika direktno iz TOKENA, ne iz JSON-a!
        User reporter = userService.getUserByUsername(principal.getName());
        report.setReporter(reporter);

        // 2. Vreme: Postavljamo vreme na serveru, ne verujemo frontendu
        report.setCreatedAt(LocalDateTime.now());

        // 3. Snimanje
        reportRepository.save(report);

        return ResponseEntity.ok(Map.of("message", "Prijava uspešno poslata. Administrator će je pregledati."));
    }
}