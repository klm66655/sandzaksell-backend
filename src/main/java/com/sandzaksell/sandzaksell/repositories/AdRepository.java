package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Ad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdRepository extends JpaRepository<Ad, Long> {
    // Ovo će ti trebati za filtriranje na frontendu
    List<Ad> findByLocationIgnoreCase(String location);
    List<Ad> findByCategoryId(Long categoryId);
    List<Ad> findByUserId(Long userId);
}