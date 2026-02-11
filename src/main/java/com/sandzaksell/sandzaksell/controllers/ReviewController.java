package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Review;
import com.sandzaksell.sandzaksell.services.ReviewService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request) {
        try {
            Review savedReview = reviewService.addReview(
                    request.getReviewerId(),
                    request.getReviewedUserId(),
                    request.getRating(),
                    request.getComment()
            );
            return ResponseEntity.ok(savedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

@Data
class ReviewRequest {
    private Long reviewerId;
    private Long reviewedUserId;
    private Integer rating;
    private String comment;
}