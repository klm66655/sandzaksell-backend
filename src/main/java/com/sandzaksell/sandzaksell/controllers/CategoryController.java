package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Category;
import com.sandzaksell.sandzaksell.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor // Koristi Lombok da smanjiš kod
@CrossOrigin(origins = {"http://localhost:5173", "https://sandzak-sell-marketplace.vercel.app"})
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public List<Category> getAll() {
        return categoryService.getAllCategories();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // SAMO ADMIN kreira
    public Category create(@RequestBody Category category) {
        return categoryService.saveCategory(category);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // SAMO ADMIN briše
    public void delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}