package com.sandzaksell.sandzaksell.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Olakšaće nam kreiranje poruka u servisu
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // MODERNE FUNKCIJE:

    @Column(name = "is_read")
    private boolean isRead = false; // Postaje true kad korisnik otvori chat (SEEN)

    @Column(name = "is_delivered")
    private boolean isDelivered = false; // Postaje true kad WebSocket isporuči poruku (DELIVERED)

    // Pomoćna polja za frontend (ne idu u bazu, ali pomažu kod slanja ID-eva)
    @Transient
    private Long senderId;
    @Transient
    private Long receiverId;
}