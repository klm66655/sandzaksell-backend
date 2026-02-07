package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.AdRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final UserRepository userRepository; // Dodajemo repozitorijum korisnika

    public List<Ad> getAllAds() { return adRepository.findAll(); }

    public Ad getAdById(Long id) {
        return adRepository.findById(id).orElseThrow(() -> new RuntimeException("Oglas nije nađen"));
    }

    @Transactional
    public Ad saveAd(Ad ad) {
        // 1. Pronađi kompletnog korisnika iz baze
        User user = userRepository.findById(ad.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        if (ad.getId() == null) {
            ad.setCreatedAt(LocalDateTime.now());
        }

        // 2. Proveri koliko slika stiže uz oglas
        int numberOfImages = ad.getImages().size();

        // 3. Logika za naplatu
        if (numberOfImages > 5) {
            if (user.getTokenBalance() < 50) {
                throw new RuntimeException("Nemate dovoljno tokena za više od 5 slika!");
            }
            // Skini tokene
            user.setTokenBalance(user.getTokenBalance() - 50);
            userRepository.save(user); // Sačuvaj novo stanje korisnika
            System.out.println("Skinuto 50 tokena korisniku: " + user.getUsername());
        }

        return adRepository.save(ad);
    }

    public void deleteAd(Long id) { adRepository.deleteById(id); }

    public List<Ad> getAdsByUserId(Long userId) {
        return adRepository.findByUserId(userId);
    }

    public List<Ad> getAdsByLocation(String location) {
        return adRepository.findByLocationIgnoreCase(location);
    }

    // Dodaj ovo u AdService.java

    @Transactional
    public Ad incrementViews(Long adId, Long userId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Oglas nije nađen"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        // Provera: Ako korisnik NIJE u setu onih koji su videli oglas
        if (!ad.getViewedByUsers().contains(user)) {
            ad.getViewedByUsers().add(user); // Dodaj ga u listu
            ad.setViews(ad.getViews() + 1);  // Povećaj broj pregleda
            return adRepository.save(ad);
        }

        return ad; // Vrati oglas bez promena ako je već gledao
    }

    @Transactional
    public Ad setAdPremium(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oglas nije nađen"));

        if (ad.getIsPremium()) {
            throw new RuntimeException("Oglas je već premium!");
        }

        User user = ad.getUser(); // Uzimamo vlasnika oglasa
        int cenaPremiuma = 100;

        if (user.getTokenBalance() < cenaPremiuma) {
            throw new RuntimeException("Nemate dovoljno tokena za Premium (potrebno 100 ST)!");
        }

        // Skini tokene i snimi
        user.setTokenBalance(user.getTokenBalance() - cenaPremiuma);
        userRepository.save(user);

        ad.setIsPremium(true);
        return adRepository.save(ad);
    }
}