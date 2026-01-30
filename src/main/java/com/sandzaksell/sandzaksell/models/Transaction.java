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
    private User user; // Korisnik koji je kupio tokene

    @Column(nullable = false)
    private Integer amount; // Broj tokena

    @Column(name = "stripe_payment_id")
    private String stripePaymentId; // ID transakcije sa Stripe-a radi provere

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
