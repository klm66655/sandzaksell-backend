package com.sandzaksell.sandzaksell.services;

import com.sandzaksell.sandzaksell.models.Review;
import com.sandzaksell.sandzaksell.models.User;
import com.sandzaksell.sandzaksell.repositories.ReviewRepository;
import com.sandzaksell.sandzaksell.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
    }

    public List<Review> getReviewsForUser(Long userId) {
        return reviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId);
    }

    public Review addReview(Long reviewerId, Long reviewedUserId, Integer rating, String comment) {
        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        User reviewed = userRepository.findById(reviewedUserId)
                .orElseThrow(() -> new RuntimeException("Reviewed user not found"));

        if (reviewRepository.existsByReviewerIdAndReviewedUserId(reviewerId, reviewedUserId)) {
            throw new RuntimeException("Već ste ocenili ovog korisnika.");
        }

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setReviewedUser(reviewed);
        review.setRating(rating);
        review.setComment(comment);

        return reviewRepository.save(review);
    }
}