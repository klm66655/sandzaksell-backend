package com.sandzaksell.sandzaksell.models;

import com.sandzaksell.sandzaksell.repositories.AdRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@EnableScheduling // Možeš i ovde staviti ili u glavnu klasu
public class AdScheduler {

    private final AdRepository adRepository;

    @Scheduled(fixedRate = 3600000) // Proverava svakih sat vremena
    @Transactional
    public void clearExpiredPremium() {
        // Tražimo sve oglase kojima je istekao premiumUntil
        List<Ad> expired = adRepository.findAllByIsPremiumTrueAndPremiumUntilBefore(LocalDateTime.now());

        for (Ad ad : expired) {
            ad.setIsPremium(false);
            ad.setPremiumUntil(null);
        }
        adRepository.saveAll(expired);
    }
}