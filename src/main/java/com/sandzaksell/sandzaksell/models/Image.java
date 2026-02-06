package com.sandzaksell.sandzaksell.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500) // Cloudinary URL-ovi mogu biti dugi
    private String url;

    @ManyToOne(fetch = FetchType.LAZY) // Optimizacija: ne vuci oglas svaki put kad ti treba samo slika
    @JoinColumn(name = "ad_id")
    @JsonIgnore // KLJUČNO: Sprečava beskonačnu petlju pri slanju podataka u React
    private Ad ad;
}