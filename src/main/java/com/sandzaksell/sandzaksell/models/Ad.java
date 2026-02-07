package com.sandzaksell.sandzaksell.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;

@Entity
@Table(name = "ads")
@Data
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
    private String location;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // OVO JE DODATO: Rešava ERROR: update or delete on table "ads" violates foreign key constraint
    @JsonIgnore
    @OneToMany(mappedBy = "ad", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports = new ArrayList<>();

    private int views = 0; // Osnovni brojač

    @ManyToMany
    @JoinTable(
            name = "ad_views",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore // Ne želimo da šaljemo čitavu listu korisnika u JSON-u, samo broj
    private java.util.Set<User> viewedByUsers = new java.util.HashSet<>();

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
}