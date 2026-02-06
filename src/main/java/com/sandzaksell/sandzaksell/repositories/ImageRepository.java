package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    // Ovde možemo dodati metodu da obrišemo sve slike jednog oglasa ako treba
    void deleteByAdId(Long adId);
}