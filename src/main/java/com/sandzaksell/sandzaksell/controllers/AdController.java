package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.AdService;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import com.sandzaksell.sandzaksell.models.Notification;
import com.sandzaksell.sandzaksell.repositories.NotificationRepository;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@CrossOrigin(
        origins = {"http://localhost:5173", "https://sandzak-sell-marketplace.vercel.app"},
        allowCredentials = "true"
)
public class AdController {

    private final AdService adService;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @GetMapping
    public List<Ad> getAll() { return adService.getAllAds(); }

    @GetMapping("/{id}")
    public Ad getOne(@PathVariable Long id) { return adService.getAdById(id); }

    @GetMapping("/search")
    public List<Ad> getByCity(@RequestParam String location) {
        return adService.getAdsByLocation(location);
    }

    @PostMapping
    public Ad create(@RequestBody Ad ad, Principal principal) {
        if (principal == null) throw new RuntimeException("Niste ulogovani!");

        // Uzimamo usera preko username-a iz tokena
        User realUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        ad.setUser(realUser);
        ad.setIsPremium(false);
        return adService.saveAd(ad);
    }

    @PutMapping("/{id}")
    public Ad updateAd(@PathVariable Long id, @RequestBody Ad adDetails, Principal principal) {
        if (principal == null) throw new RuntimeException("Niste ulogovani!");

        Ad existingAd = adService.getAdById(id);
        String currentUsername = principal.getName();

        if (!existingAd.getUser().getUsername().equals(currentUsername) && !isUserAdmin(principal)) {
            throw new RuntimeException("Nemaš dozvolu da menjaš ovaj oglas!");
        }

        existingAd.setTitle(adDetails.getTitle());
        existingAd.setPrice(adDetails.getPrice());
        existingAd.setDescription(adDetails.getDescription());
        existingAd.setLocation(adDetails.getLocation());


        return adService.saveAd(existingAd);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Principal principal) {
        if (principal == null) throw new RuntimeException("Niste ulogovani!");

        Ad existingAd = adService.getAdById(id);
        if (existingAd.getUser().getUsername().equals(principal.getName()) || isUserAdmin(principal)) {
            adService.deleteAd(id);
        } else {
            throw new RuntimeException("Nemaš dozvolu za brisanje!");
        }
    }

    // POPRAVLJENO: @PreAuthorize zahteva @EnableMethodSecurity u SecurityConfig-u
    @PutMapping("/{id}/make-premium")
    public ResponseEntity<?> makePremium(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Niste ulogovani!");

        Ad ad = adService.getAdById(id);

        // Provjeri da li je vlasnik oglasa
        if (!ad.getUser().getUsername().equals(principal.getName())) {
            return ResponseEntity.status(403).body("Ne možeš platiti premium za tuđi oglas!");
        }

        try {
            Ad updatedAd = adService.setAdPremium(id);
            return ResponseEntity.ok(updatedAd);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public List<Ad> getAdsByUser(@PathVariable Long userId) {
        return adService.getAdsByUserId(userId);
    }

    // POPRAVLJENO: Sada šalje ispravne parametre servisu
    @PostMapping("/{id}/view")
    public Ad trackView(@PathVariable Long id, Principal principal) {
        Long authenticatedUserId = null;

        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                authenticatedUserId = user.getId();
            }
        }
        return adService.incrementViews(id, authenticatedUserId);
    }

    private boolean isUserAdmin(Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken)) return false;
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    @Transactional
    @PostMapping("/favorite/{id}")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).body("Moraš biti ulogovan!");

        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        Ad ad = adService.getAdById(id);

        // Proveravamo da li je već u favoritima
        boolean alreadyFavorite = currentUser.getFavoriteAds().stream()
                .anyMatch(fav -> fav.getId().equals(id));

        if (alreadyFavorite) {
            // 1. UKLANJANJE
            currentUser.getFavoriteAds().removeIf(fav -> fav.getId().equals(id));
            userRepository.save(currentUser);
            return ResponseEntity.ok("Uklonjeno iz omiljenih");
        } else {
            // 2. DODAVANJE
            currentUser.getFavoriteAds().add(ad);
            userRepository.save(currentUser);

            // 3. KREIRANJE NOTIFIKACIJE (Samo ako nije tvoj oglas)
            if (!ad.getUser().getId().equals(currentUser.getId())) {
                Notification notification = new Notification();
                notification.setUser(ad.getUser()); // Vlasnik oglasa dobija notifikaciju
                notification.setMessage("Korisnik " + currentUser.getUsername() + " je sačuvao vaš oglas: " + ad.getTitle());
                notification.setType("FAVORITE");
                notification.setRelatedId(ad.getId());
                notification.setIsRead(false);
                notification.setCreatedAt(java.time.LocalDateTime.now());

                notificationRepository.save(notification);
            }

            return ResponseEntity.ok("Dodato u omiljene");
        }
    }

    // Ruta da korisnik vidi SVE svoje omiljene oglase
    @GetMapping("/favorites")
    public ResponseEntity<java.util.Set<Ad>> getMyFavorites(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userRepository.findByUsername(principal.getName()).get();
        return ResponseEntity.ok(user.getFavoriteAds());
    }


}