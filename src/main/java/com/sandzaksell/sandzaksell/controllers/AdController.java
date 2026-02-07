package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.services.AdService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ads")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "https://sandzak-sell-marketplace.vercel.app"})
public class AdController {

    // Spring vidi ovo i automatski ga ubacuje (Dependency Injection)
    private final AdService adService;

    @GetMapping
    public List<Ad> getAll() { return adService.getAllAds(); }

    @GetMapping("/{id}")
    public Ad getOne(@PathVariable Long id) { return adService.getAdById(id); }

    @GetMapping("/search")
    public List<Ad> getByCity(@RequestParam String location) {
        return adService.getAdsByLocation(location);
    }

    @PostMapping
    public Ad create(@RequestBody Ad ad) { return adService.saveAd(ad); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        // FIKSIRANO: Koristimo adService umesto adRepository
        adService.deleteAd(id);
    }

    @PutMapping("/{id}/make-premium")
    public Ad makePremium(@PathVariable Long id) {
        return adService.setAdPremium(id);
    }

    @GetMapping("/user/{userId}")
    public List<Ad> getAdsByUser(@PathVariable Long userId) {
        // Ovo pretpostavlja da u AdService imaš metodu koja filtrira po User ID-u
        return adService.getAdsByUserId(userId);
    }

    @PostMapping("/{id}/view")
    public Ad trackView(@PathVariable Long id, @RequestParam Long userId) {
        return adService.incrementViews(id, userId);
    }

    @PutMapping("/{id}")
    public Ad updateAd(@PathVariable Long id, @RequestBody Ad adDetails) {
        Ad existingAd = adService.getAdById(id);

        // Ažuriramo osnovna polja
        existingAd.setTitle(adDetails.getTitle());
        existingAd.setPrice(adDetails.getPrice());
        existingAd.setDescription(adDetails.getDescription());
        existingAd.setLocation(adDetails.getLocation());

        // Čuvamo preko postojećeg saveAd u servisu (on već ima logiku za tokene ako treba)
        return adService.saveAd(existingAd);
    }
}