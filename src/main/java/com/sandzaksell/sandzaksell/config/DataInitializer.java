package com.sandzaksell.sandzaksell.config;

import com.sandzaksell.sandzaksell.models.*;
import com.sandzaksell.sandzaksell.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public DataInitializer(UserRepository userRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Ubaci testnog Admina ako ne postoji
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("kelim_admin");
            admin.setEmail("admin@sandzaksell.com");
            admin.setPassword("sifra123"); // Kasnije ćemo ovo enkriptovati
            admin.setRole("ROLE_ADMIN");
            admin.setTokenBalance(999);
            userRepository.save(admin);
            System.out.println("✅ Admin korisnik kreiran!");
        }

        // 2. Ubaci osnovne kategorije za Sandžak Sell
        if (categoryRepository.count() == 0) {
            Category c1 = new Category(null, "Automobili", "car-icon.png", null);
            Category c2 = new Category(null, "Nekretnine", "house-icon.png", null);
            Category c3 = new Category(null, "Poljoprivreda", "tractor-icon.png", null);
            Category c4 = new Category(null, "Elektronika", "phone-icon.png", null);

            categoryRepository.saveAll(Arrays.asList(c1, c2, c3, c4));
            System.out.println("✅ Osnovne kategorije ubačene!");
        }
        if (userRepository.count() == 1) { // Ako ima samo admin, dodaj običnog usera
            User user2 = new User();
            user2.setUsername("pazarac_99");
            user2.setEmail("user@test.com");
            user2.setPassword("sifra123");
            user2.setRole("ROLE_USER");
            user2.setTokenBalance(50);
            userRepository.save(user2);
            System.out.println("✅ Drugi testni korisnik kreiran!");
        }
    }
}