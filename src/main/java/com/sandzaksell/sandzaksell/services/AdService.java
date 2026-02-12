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
        // 1. VIŠE NE VERUJEMO ID-u iz ad.getUser().getId() jer se može lažirati!
        // Uzimamo korisnika koji je već setovan u kontroleru preko Principal-a
        User user = userRepository.findByUsername(ad.getUser().getUsername())
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        if (ad.getId() == null) {
            ad.setCreatedAt(LocalDateTime.now());
        }

        // 2. Provera slika (dodajemo null check da ne pukne)
        int numberOfImages = (ad.getImages() != null) ? ad.getImages().size() : 0;

        // 3. Sigurna naplata - skidamo tokene SAMO onome ko je vlasnik oglasa
        if (numberOfImages > 5) {
            if (user.getTokenBalance() < 50) {
                throw new RuntimeException("Nemate dovoljno tokena za više od 5 slika!");
            }
            user.setTokenBalance(user.getTokenBalance() - 50);
            userRepository.save(user);
        }

        // Setujemo osvežen user objekat nazad u oglas pre čuvanja
        ad.setUser(user);
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

        // Ako korisnik nije ulogovan (userId je null), samo uvećaj ukupan broj pregleda
        if (userId == null) {
            ad.setViews((ad.getViews() == null ? 0 : ad.getViews()) + 1);
            return adRepository.save(ad);
        }

        // Ako je ulogovan, proveri da li je već gledao oglas (unique views)
        User user = userRepository.findById(userId).orElse(null);

        if (user != null && !ad.getViewedByUsers().contains(user)) {
            ad.getViewedByUsers().add(user);
            ad.setViews((ad.getViews() == null ? 0 : ad.getViews()) + 1);
            return adRepository.save(ad);
        }

        return ad;
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