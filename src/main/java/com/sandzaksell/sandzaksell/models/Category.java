package com.sandzaksell.sandzaksell.models;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // npr. "Elektronika", "Nekretnine"

    @Column(name = "icon_url")
    private String iconUrl; // Link do ikonice za React frontend

    // Jedna kategorija ima mnogo oglasa
    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<Ad> ads;
}