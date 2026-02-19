package com.sandzaksell.sandzaksell.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*; // DODATO: Paket za validaciju
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

    @JsonIgnore
    private String googleId;

    @NotBlank(message = "Username ne sme biti prazan")
    @Size(min = 3, max = 20, message = "Username mora imati između 3 i 20 karaktera")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email je obavezan")
    @Email(message = "Format email adrese nije validan") // Proverava @ i domen
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Lozinka je obavezna")
    @Size(min = 8, message = "Lozinka mora imati najmanje 8 karaktera")
    // Opciono: Dodaj @Pattern ako želiš da forsiraš broj ili veliko slovo
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    private String role = "ROLE_USER";

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "token_balance")
    private Integer tokenBalance = 0;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @JsonIgnore
    @Column(name = "reset_code")
    private String resetCode;

    @JsonIgnore
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