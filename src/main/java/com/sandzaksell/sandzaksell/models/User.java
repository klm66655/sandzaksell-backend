package com.sandzaksell.sandzaksell.models;

import java.util.*;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter // Umesto @Data
@Setter // Umesto @Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    private String googleId;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username ne može biti prazan!")
    @Size(min = 3, max = 30, message = "Username mora biti između 3 i 30 karaktera!")
    private String username;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "Email ne može biti prazan!")
    @Email(message = "Email nije validan!")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    @NotBlank(message = "Lozinka ne može biti prazna!")
    @Size(min = 9, message = "Lozinka mora imati bar 9 karaktera!")
    private String password;


    @JsonIgnore
    private String role = "ROLE_USER";

    @JsonIgnore
    @Column(name = "token_balance")
    private Integer tokenBalance = 0;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @ManyToMany
    @JoinTable(
            name = "user_favorites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "ad_id")
    )
    @JsonIgnoreProperties("user") // KLJUČNO: Kad listaš favorite, nemoj unutar svakog oglasa opet učitavati celog usera
    private Set<Ad> favoriteAds = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Ad> ads;

    @JsonIgnore
    private String resetCode;
    @JsonIgnore
    private LocalDateTime resetCodeExpiresAt;

    @JsonIgnore
    private Boolean enabled = true;
    private String phone;

    @JsonIgnore
    @OneToMany(mappedBy = "reviewedUser", cascade = CascadeType.ALL)
    private List<Review> reviewsReceived;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL)
    private List<Review> reviewsGiven;

    @JsonIgnore
    @OneToMany(mappedBy = "reportedUser", cascade = CascadeType.ALL)
    private List<UserReport> reportsReceived = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id != null && id.equals(((User) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}