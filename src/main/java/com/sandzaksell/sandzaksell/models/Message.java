package com.sandzaksell.sandzaksell.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties(ignoreUnknown = true, value = {"messages", "ads", "favorites", "password", "email", "tokens", "role", "tokenBalance", "username", "phone", "profileImageUrl", "enabled", "googleId", "resetCode", "resetCodeExpiresAt", "favoriteAds", "reviewsReceived", "reviewsGiven", "notifications", "reportsReceived"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnoreProperties(ignoreUnknown = true, value = {"messages", "ads", "favorites", "password", "email", "tokens", "role", "tokenBalance", "username", "phone", "profileImageUrl", "enabled", "googleId", "resetCode", "resetCodeExpiresAt", "favoriteAds", "reviewsReceived", "reviewsGiven", "notifications", "reportsReceived"})
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Builder.Default  // OVO DODAJ
    @Column(name = "timestamp", nullable = false, updatable = false) // updatable = false je bitno!
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default  // OVO DODAJ
    @Column(name = "is_read")
    private Boolean read = false;

    @Builder.Default  // OVO DODAJ
    @Column(name = "is_delivered")
    private Boolean delivered = false;

    @JsonIgnore
    @Transient
    private Long senderId;

    @JsonIgnore
    @Transient
    private Long receiverId;
}