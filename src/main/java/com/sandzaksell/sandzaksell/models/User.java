package com.sandzaksell.sandzaksell.models;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonIgnore // Haker ne mora da zna tvoj interni Google ID
    private String googleId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String role = "ROLE_USER";

    @JsonProperty(access = JsonProperty.Access.READ_ONLY) // Može da se čita (vidi na sajtu), ali ne može da se "podmetne" u requestu
    @Column(name = "token_balance")
    private Integer tokenBalance = 0;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    // NOVI DEO ZA RESET LOZINKE - OBAVEZNO IGNORE
    @JsonIgnore // Da ne bi mogao da "presretne" kod za reset u JSON-u
    @Column(name = "reset_code")
    private String resetCode;

    @JsonIgnore // Ovo je interna informacija
    @Column(name = "reset_code_expires_at")
    private LocalDateTime resetCodeExpiresAt;

    @Column(name = "enabled")
    private Boolean enabled = true;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ad> ads;

    @JsonIgnore
    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL)
    private List<Review> reviewsReceived;

    @JsonIgnore
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> reviewsGiven;


    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "phone")
    private String phone;

    public Double getAverageRating() {
        if (reviewsReceived == null || reviewsReceived.isEmpty()) return 0.0;
        return reviewsReceived.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}