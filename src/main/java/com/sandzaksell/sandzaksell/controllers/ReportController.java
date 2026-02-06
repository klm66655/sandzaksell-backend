package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Report;
import com.sandzaksell.sandzaksell.repositories.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "http://localhost:3000")
public class ReportController {

    @Autowired
    private ReportRepository reportRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendReport(@RequestBody Report report) {
        // Ovde možeš dodati logiku da proveriš da li oglas postoji pre snimanja
        reportRepository.save(report);
        return ResponseEntity.ok("Prijava uspešno poslata.");
    }
}