package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Ad;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.AdRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sandzaksell.sandzaksell.models.Notification;
import com.sandzaksell.sandzaksell.repositories.NotificationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdService {
    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    // 1. POPRAVLJENO: Vuče samo oglase korisnika koji NISU banovani
    public List<Ad> getAllAds() {
        return adRepository.findAllByUserEnabledTrue();
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

        // NOVO: Sigurnosna provera - ako je korisnik banovan, ne daj mu da snimi oglas!
        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new RuntimeException("PRISTUP ODBIJEN: Vaš nalog je banovan i ne možete objavljivati oglase.");
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

    // 2. POPRAVLJENO: Filtrira banovane korisnike na osnovu lokacije
    public List<Ad> getAdsByLocation(String location) {
        return adRepository.findByLocationIgnoreCaseAndUserEnabledTrue(location);
    }

    // Dodatna metoda ako ti treba za kategorije (samo enabled korisnici)
    public List<Ad> getAdsByCategoryId(Long categoryId) {
        return adRepository.findByCategoryIdAndUserEnabledTrue(categoryId);
    }

    @Transactional
    public Ad incrementViews(Long adId, Long userId) {
        Ad ad = getAdById(adId);

        if (userId == null) {
            ad.setViews((ad.getViews() == null ? 0 : ad.getViews()) + 1);
            return adRepository.save(ad);
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
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

        if (Boolean.TRUE.equals(ad.getIsPremium())) {
            throw new RuntimeException("Oglas je već u statusu HITNA PRODAJA!");
        }

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new RuntimeException("Ne možete kupiti premium jer je vaš nalog suspendovan.");
        }

        int cenaPremiuma = 100;
        if (user.getTokenBalance() < cenaPremiuma) {
            throw new RuntimeException("Nedovoljno tokena (potrebno 100 ST)!");
        }

        user.setTokenBalance(user.getTokenBalance() - cenaPremiuma);
        userRepository.save(user);

        ad.setIsPremium(true);
        // Postavljamo na 2 dana (možeš staviti .plusDays(2))
        ad.setPremiumUntil(LocalDateTime.now().plusDays(2));

        return adRepository.save(ad);
    }


}