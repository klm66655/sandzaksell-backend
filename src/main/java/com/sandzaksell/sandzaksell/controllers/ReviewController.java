package com.sandzaksell.sandzaksell.controllers;

import com.sandzaksell.sandzaksell.models.Review;
import com.sandzaksell.sandzaksell.services.ReviewService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor // Automatski ubacuje ReviewService
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Review>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addReview(@RequestBody ReviewRequest request, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Morate biti ulogovani.");
        }

        try {
            // KLJUČNA PROMENA: Šaljemo principal.getName() (što je String/Username)
            // umesto request.getReviewerId()
            Review savedReview = reviewService.addReview(
                    principal.getName(),
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

// DTO klasa - usklađena sa Frontendom (bez reviewerId jer ide iz tokena)
@Data
class ReviewRequest {
    private Long reviewedUserId;
    private Integer rating;
    private String comment;
}