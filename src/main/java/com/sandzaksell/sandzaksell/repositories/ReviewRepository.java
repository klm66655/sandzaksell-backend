package com.sandzaksell.sandzaksell.repositories;

import com.sandzaksell.sandzaksell.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // Izvlači sve recenzije za određenog korisnika (onog na čijem smo profilu)
    List<Review> findByReviewedUserIdOrderByCreatedAtDesc(Long userId);

    // Provera da li je korisnik već jednom ocenio ovog trgovca (da ne duplira)
    boolean existsByReviewerIdAndReviewedUserId(Long reviewerId, Long reviewedUserId);
}