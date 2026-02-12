package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Category;
import com.sandzaksell.sandzaksell.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category saveCategory(Category category) {
        // Provera da li kategorija sa tim imenom već postoji
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Kategorija sa tim imenom već postoji!");
        }
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category cat = categoryRepository.findById(id).orElseThrow();
        if (cat.getAds() != null && !cat.getAds().isEmpty()) {
            throw new RuntimeException("Ne možete obrisati kategoriju koja sadrži oglase!");
        }
        categoryRepository.deleteById(id);
    }
}