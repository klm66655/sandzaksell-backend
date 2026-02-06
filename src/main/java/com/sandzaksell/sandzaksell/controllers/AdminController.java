package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.Report;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.AdRepository;
import com.sandzaksell.sandzaksell.repositories.ReportRepository;
import com.sandzaksell.sandzaksell.services.AdService;
import com.sandzaksell.sandzaksell.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private AdRepository adRepository;

    @Autowired
    private final AdService adService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private UserService userService;

    // 1. Obriši bilo koji oglas
    @DeleteMapping("/ads/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        adRepository.deleteById(id);
        return ResponseEntity.ok("Oglas obrisan");
    }

    // 2. Postavi oglas kao Premium
    @PutMapping("/ads/{id}/premium")
    public ResponseEntity<?> makePremium(@PathVariable Long id) {
        Ad ad = adRepository.findById(id).orElseThrow(() -> new RuntimeException("Oglas nije nađen"));
        ad.setIsPremium(true);
        adRepository.save(ad);
        return ResponseEntity.ok("Oglas je sada premium");
    }

    // 3. Dodaj tokene korisniku
    @PutMapping("/users/{id}/add-tokens")
    public ResponseEntity<?> addTokens(@PathVariable Long id, @RequestBody Integer amount) {
        userService.updateTokens(id, amount);
        return ResponseEntity.ok("Tokeni uspešno dodati");
    }

    // 4. Uzmi sve prijave (za Dashboard)
    @GetMapping("/reports")
    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByCreatedAtDesc();
    }

    // 5. BAN KORISNIKA - Pozivamo servis
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long id) {
        userService.banUser(id);
        return ResponseEntity.ok("Korisnik sa ID " + id + " je banovan.");
    }

    @PutMapping("/ads/{id}/set-premium")
    public ResponseEntity<Ad> setPremium(@PathVariable Long id) {
        // Pozivamo metodu iz servisa koju smo gore napravili
        Ad updatedAd = adService.setAdPremium(id);
        return ResponseEntity.ok(updatedAd);
    }



    // 6. Odbaci prijavu
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<?> dismissReport(@PathVariable Long id) {
        reportRepository.deleteById(id);
        return ResponseEntity.ok("Prijava je odbačena");
    }
}