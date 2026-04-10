package com.sandzaksell.sandzaksell.models;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.*;
import java.util.*;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Entity
@Table(name = "ads")
@Getter // Umesto @Data
@Setter // Umesto @Data
@NoArgsConstructor
@AllArgsConstructor
public class Ad {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    @Column(name = "currency", length = 3)
    private String currency = "EUR";

    private String location;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime premiumUntil;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"ads", "favoriteAds", "password", "email", "tokenBalance"}) // KLJUČNO: Ne povlači sve o vlasniku ovde
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    private Integer views = 0;
    private Boolean isUsed = false;

    @ManyToMany
    @JoinTable(
            name = "ad_views",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private Set<User> viewedByUsers = new HashSet<>();

    @JsonProperty("imageUrls")
    public void setImageUrls(List<String> urls) {
        if (urls != null) {
            this.images = urls.stream().map(url -> {
                Image img = new Image();
                img.setUrl(url);
                img.setAd(this);
                return img;
            }).collect(Collectors.toList());
        }
    }

    // Ručno dodajemo hashCode i equals samo preko ID-a da izbegnemo petlju
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ad)) return false;
        return id != null && id.equals(((Ad) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}