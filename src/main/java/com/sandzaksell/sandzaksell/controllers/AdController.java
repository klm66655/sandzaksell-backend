package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.services.AdService;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
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

        // 1. principal.getName() u tvom sistemu je USERNAME
        String username = principal.getName();

        // 2. Tražimo preko findByUsername (isto kao što radiš u ReviewController-u)
        User realUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Korisnik sa username-om " + username + " nije nađen"));

        ad.setUser(realUser);
        ad.setIsPremium(false);

        return adService.saveAd(ad);
    }

    @PutMapping("/{id}")
    public Ad updateAd(@PathVariable Long id, @RequestBody Ad adDetails, Principal principal) {
        Ad existingAd = adService.getAdById(id);
        String currentUsername = principal.getName(); // Username iz tokena

        // POPRAVKA: Poredimo username sa username-om, a ne emailom!
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
        Ad existingAd = adService.getAdById(id);
        String currentUsername = principal.getName();

        // POPRAVKA: Ovde isto poredimo username!
        if (existingAd.getUser().getUsername().equals(currentUsername) || isUserAdmin(principal)) {
            adService.deleteAd(id);
        } else {
            throw new RuntimeException("Nemaš dozvolu za brisanje!");
        }
    }

    @PutMapping("/{id}/make-premium")
    public Ad makePremium(@PathVariable Long id, Principal principal) {
        // Ovde dopuštamo samo adminu da nekoga proglasi premiumom
        if (!isUserAdmin(principal)) {
            throw new RuntimeException("Samo admin može da dodeli premium status!");
        }
        return adService.setAdPremium(id);
    }

    @GetMapping("/user/{userId}")
    public List<Ad> getAdsByUser(@PathVariable Long userId) {
        return adService.getAdsByUserId(userId);
    }

    @PostMapping("/{id}/view")
    public Ad trackView(@PathVariable Long id, @RequestParam Long userId) {
        return adService.incrementViews(id, userId);
    }

    // POMOĆNA METODA: Proverava uloge iz tokena
    private boolean isUserAdmin(Principal principal) {
        if (!(principal instanceof UsernamePasswordAuthenticationToken)) return false;
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}