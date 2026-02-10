package com.sandzaksell.sandzaksell.services;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.List;

@Service
public class EmailService {

    public void sendResetEmail(String to, String code) {
        // Povlačimo ključ iz Railway varijabli
        String apiKey = System.getenv("BREVO_API_KEY");
        String url = "https://api.brevo.com/v3/smtp/email";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        // Kreiramo JSON telo zahteva prema Brevo API dokumentaciji
        Map<String, Object> body = Map.of(
                "sender", Map.of("email", "kelimselmanovic123@gmail.com", "name", "Sandzak Sell"),
                "to", List.of(Map.of("email", to)),
                "subject", "Kod za reset lozinke",
                "htmlContent", "<h3>Vaš kod za reset lozinke je: <strong>" + code + "</strong></h3>"
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(url, entity, String.class);
            System.out.println("MEJL USPEŠNO POSLAT PREKO API-JA!");
        } catch (Exception e) {
            System.err.println("GREŠKA PRI SLANJU PREKO API-JA: " + e.getMessage());
        }
    }
}