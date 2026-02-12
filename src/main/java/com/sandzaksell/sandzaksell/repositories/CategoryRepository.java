package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
    // Ovde već imamo sve osnovne metode poput findAll(), save(), delete()...
}