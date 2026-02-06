package com.sandzaksell.sandzaksell.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer amount; // Broj tokena (npr. 100)

    @Column(name = "paypal_order_id", unique = true)
    private String paypalOrderId; // ID koji dobiješ iz details.id u Reactu

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}