package com.sandzaksell.sandzaksell.models;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role = "ROLE_USER";

    @Column(name = "token_balance")
    private Integer tokenBalance = 0;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // NOVI DEO ZA RESET LOZINKE
    @Column(name = "reset_code")
    private String resetCode;

    @Column(name = "reset_code_expires_at")
    private LocalDateTime resetCodeExpiresAt;

    @Column(name = "enabled")
    private Boolean enabled = true; // Korisnik je aktivan po defaultu

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ad> ads;

    @JsonIgnore
    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL)
    private List<Review> reviewsReceived; // Recenzije koje su drugi ostavili ovom korisniku

    @JsonIgnore
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> reviewsGiven; // Recenzije koje je ovaj korisnik ostavio drugima

    // Pomoćna metoda za računanje prosečne ocene
    public Double getAverageRating() {
        if (reviewsReceived == null || reviewsReceived.isEmpty()) return 0.0;
        return reviewsReceived.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}