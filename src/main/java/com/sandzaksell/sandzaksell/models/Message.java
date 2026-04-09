package com.sandzaksell.sandzaksell.models;

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
    @JsonIgnoreProperties({"messages", "ads", "favorites", "password", "email", "tokens", "role", "tokenBalance"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnoreProperties({"messages", "ads", "favorites", "password", "email", "tokens", "role", "tokenBalance"})
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "is_read")
    private Boolean read = false;

    @Column(name = "is_delivered")
    private Boolean delivered = false;

    @Transient
    private Long senderId;
    @Transient
    private Long receiverId;
}