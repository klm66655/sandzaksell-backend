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
    private final UserRepository userRepository;

    public List<Ad> getAllAds() {
        return adRepository.findAll();
    }

    public Ad getAdById(Long id) {
        return adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oglas sa ID " + id + " nije nađen"));
    }

    @Transactional
    public Ad saveAd(Ad ad) {
        // 1. Sigurnost: Koristimo user objekat koji nam je Controller već spremio
        User user = ad.getUser();
        if (user == null) {
            throw new RuntimeException("Sistemska greška: Korisnik oglasa nije setovan!");
        }

        // 2. Ako je novi oglas, postavi vreme i inicijalizuj preglede
        if (ad.getId() == null) {
            ad.setCreatedAt(LocalDateTime.now());
            ad.setViews(0);
        }

        // 3. Sigurna naplata za slike (Preko 5 slika košta 50 tokena)
        int numberOfImages = (ad.getImages() != null) ? ad.getImages().size() : 0;
        if (numberOfImages > 5) {
            int tokenPrice = 50;
            if (user.getTokenBalance() < tokenPrice) {
                throw new RuntimeException("Nedovoljno tokena! Imate " + user.getTokenBalance() + ", a treba vam " + tokenPrice + " za više od 5 slika.");
            }
            user.setTokenBalance(user.getTokenBalance() - tokenPrice);
            userRepository.save(user); // Čuvamo novo stanje tokena
        }

        return adRepository.save(ad);
    }

    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }

    public List<Ad> getAdsByUserId(Long userId) {
        return adRepository.findByUserId(userId);
    }

    public List<Ad> getAdsByLocation(String location) {
        return adRepository.findByLocationIgnoreCase(location);
    }

    @Transactional
    public Ad incrementViews(Long adId, Long userId) {
        Ad ad = getAdById(adId);

        // Ako je userId null, posetilac je anoniman
        if (userId == null) {
            ad.setViews((ad.getViews() == null ? 0 : ad.getViews()) + 1);
            return adRepository.save(ad);
        }

        // Ako je korisnik ulogovan, proveri da li je već gledao (Unique Views)
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            // Provera da li je user već u listi onih koji su videli oglas
            boolean alreadyViewed = ad.getViewedByUsers().stream()
                    .anyMatch(u -> u.getId().equals(userId));

            if (!alreadyViewed) {
                ad.getViewedByUsers().add(user);
                ad.setViews((ad.getViews() == null ? 0 : ad.getViews()) + 1);
                return adRepository.save(ad);
            }
        }

        return ad;
    }

    @Transactional
    public Ad setAdPremium(Long id) {
        Ad ad = getAdById(id);
        User user = ad.getUser();

        // SIGURNOST 1: Provera da li je već premium (da mu ne skine pare dva puta)
        if (Boolean.TRUE.equals(ad.getIsPremium())) {
            throw new RuntimeException("Oglas je već u statusu HITNA PRODAJA!");
        }

        // SIGURNOST 2: Provera da li je korisnik banovan pre nego što mu uzmemo pare
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new RuntimeException("Ne možete kupiti premium jer je vaš nalog suspendovan.");
        }

        int cenaPremiuma = 100;
        if (user.getTokenBalance() < cenaPremiuma) {
            throw new RuntimeException("Nedovoljno tokena (potrebno 100 ST)!");
        }

        // 1. Skidanje tokena
        user.setTokenBalance(user.getTokenBalance() - cenaPremiuma);
        userRepository.save(user);

        // 2. Postavljanje statusa i DATUMA ISTEKA (2 dana = 48 sati)
        ad.setIsPremium(true);
        ad.setPremiumUntil(LocalDateTime.now().plusDays(1));

        return adRepository.save(ad);
    }
}