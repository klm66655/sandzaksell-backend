package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.AdService;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://sandzak-sell-marketplace.vercel.app"})
public class AdController {

    private final AdService adService;
    private final UserRepository userRepository;

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
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/make-premium")
    public Ad makePremium(@PathVariable Long id) {
        return adService.setAdPremium(id);
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
}