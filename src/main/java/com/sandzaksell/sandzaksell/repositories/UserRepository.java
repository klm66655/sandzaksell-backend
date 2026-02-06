package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // OBAVEZNO uvezi ovo

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // OVO JE KLJUČNO: Mora biti Optional
    Optional<User> findByUsername(String username);
}