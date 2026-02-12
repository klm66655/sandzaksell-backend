package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Review;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.ReviewRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public List<Review> getReviewsForUser(Long userId) {
        return reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId);
    }

    public Review addReview(String reviewerUsername, Long reviewedUserId, Integer rating, String comment) {
        // 1. Uzimamo recenzenta iz baze na osnovu Username-a iz TOKENA (Principal)
        User reviewer = userRepository.findByUsername(reviewerUsername)
                .orElseThrow(() -> new RuntimeException("Korisnik nije nađen"));

        User reviewed = userRepository.findById(reviewedUserId)
                .orElseThrow(() -> new RuntimeException("Ocenjivani korisnik nije nađen"));

        // 2. ZAŠTITA: Ne možeš oceniti sam sebe
        if (reviewer.getId().equals(reviewedUserId)) {
            throw new RuntimeException("Ne možete oceniti sami sebe!");
        }

        // 3. ZAŠTITA: Opseg ocene (1-5)
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("Ocena mora biti između 1 i 5.");
        }

        // 4. ZAŠTITA: Dupla recenzija (Ovo si već imao, top!)
        if (reviewRepository.existsByReviewerIdAndReviewedUserId(reviewer.getId(), reviewedUserId)) {
            throw new RuntimeException("Već ste ocenili ovog korisnika.");
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewed);
        review.setRating(rating);
        review.setComment(comment);
        review.setCreatedAt(LocalDateTime.now()); // Uvek setuj vreme na serveru

        return reviewRepository.save(review);
    }
}