package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query(value = "SELECT * FROM ads a " +
            "JOIN users u ON a.user_id = u.id " +
            "WHERE u.enabled = true AND " +
            "(6371 * acos(cos(radians(:userLat)) * cos(radians(a.latitude)) * " +
            "cos(radians(a.longitude) - radians(:userLng)) + " +
            "sin(radians(:userLat)) * sin(radians(a.latitude)))) <= :radius",
            nativeQuery = true)
    List<Ad> findNearbyAds(@Param("userLat") Double userLat,
                           @Param("userLng") Double userLng,
                           @Param("radius") Double radius);



}