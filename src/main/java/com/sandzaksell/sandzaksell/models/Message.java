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
    // OVO JE KLJUČ: Sprečava da se povuku sve poruke i reklame usera u beskonačno
    @JsonIgnoreProperties({"messages", "ads", "favorites", "password", "email", "tokens"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    @JsonIgnoreProperties({"messages", "ads", "favorites", "password", "email", "tokens"})
    private User receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    // Promeni ime polja u 'read' i 'delivered'
    // Hibernate će ih u bazi i dalje mapirati na 'is_read' i 'is_delivered' zbog anotacija
    @Column(name = "is_read")
    private boolean read = false;

    @Column(name = "is_delivered")
    private boolean delivered = false;

    @Transient
    private Long senderId;
    @Transient
    private Long receiverId;
}