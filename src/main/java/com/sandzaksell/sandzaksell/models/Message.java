package com.sandzaksell.sandzaksell.models;



import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender; // Ko šalje poruku

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private User receiver; // Kome stiže poruka

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();
}
