package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional; // OBAVEZNO uvezi ovo

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    // OVO JE KLJUČNO: Mora biti Optional
    Optional<User> findByUsername(String username);

    @Modifying
    @Query(value = "DELETE FROM user_favorites WHERE ad_id = :adId", nativeQuery = true)
    void removeAdFromAllFavorites(@Param("adId") Long adId);
}