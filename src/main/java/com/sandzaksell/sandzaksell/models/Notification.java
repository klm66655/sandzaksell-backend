package com.sandzaksell.sandzaksell.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Korisnik koji prima notifikaciju (vlasnik oglasa)

    @Column(nullable = false)
    private String message; // Poruka: "Korisnik X je sačuvao vaš oglas Y"

    private String type; // Npr. "FAVORITE", "MESSAGE", "SYSTEM"

    private Long relatedId; // ID oglasa ili poruke na koju se odnosi

    private Boolean isRead = false;

    private LocalDateTime createdAt = LocalDateTime.now();
}