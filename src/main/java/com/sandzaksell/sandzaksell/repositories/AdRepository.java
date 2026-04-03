package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {

    // 1. Glavna metoda za Home Page - vuče samo oglase AKTIVNIH korisnika
    List<Ad> findAllByUserEnabledTrue();

    // 2. Filtriranje po lokaciji, ali samo za u-redu korisnike
    List<Ad> findByLocationIgnoreCaseAndUserEnabledTrue(String location);

    // 3. Po kategoriji, ali samo od onih koji nisu banovani
    List<Ad> findByCategoryIdAndUserEnabledTrue(Long categoryId);

    // 4. Ovo ostaje isto (za profil korisnika)
    List<Ad> findByUserId(Long userId);

    // 5. Za tvoj Scheduler (da ugasi vatrene oglase)
    List<Ad> findAllByIsPremiumTrueAndPremiumUntilBefore(LocalDateTime now);
}