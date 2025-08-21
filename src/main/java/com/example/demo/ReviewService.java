package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserService userService;

    public List<Review> getReviewsForProduct(String productId, String sortBy, String jwt, String ip, String userAgent) {
        String currentEmail = userService.getEmailFromToken(jwt, ip, userAgent);
        return reviewRepository.getReviewsForProduct(productId, sortBy, currentEmail != null ? currentEmail : "");
    }

    public List<Rating> getRatingsForProduct(String productId) {
        return reviewRepository.getRatingsForProduct(productId);
    }

    public Review addReview(String productId, String reviewText, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        Long id = reviewRepository.insertReview(productId, email, reviewText);
        return getReviewsForProduct(productId, "latest", jwt, ip, userAgent).stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to add review"));
    }

    public Rating addRating(String productId, int rating, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (reviewRepository.hasRated(productId, email)) {
            throw new IllegalArgumentException("User has already rated this product");
        }
        Long id = reviewRepository.insertRating(productId, email, rating);
        return getRatingsForProduct(productId).stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Failed to add rating"));
    }

    public boolean updateRating(String productId, int newRating, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        return reviewRepository.updateRating(productId, email, newRating);
    }

    public boolean deleteRating(String productId, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        return reviewRepository.deleteRating(productId, email);
    }

    public boolean deleteReview(Long reviewId, String productId, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (!reviewRepository.reviewExistsForProduct(reviewId, productId)) {
            throw new IllegalArgumentException("Review not found or does not belong to product");
        }
        return reviewRepository.deleteReview(reviewId, email);
    }

    public void vote(Long reviewId, String voteType, String jwt, String ip, String userAgent) {
        String email = validateUser(jwt, ip, userAgent);
        if (!"like".equals(voteType) && !"dislike".equals(voteType)) {
            throw new IllegalArgumentException("Invalid vote type");
        }
        reviewRepository.upsertVote(reviewId, email, voteType);
    }

    private String validateUser(String jwt, String ip, String userAgent) {
        if (!userService.checkTokenValidity(jwt, ip, userAgent)) {
            throw new SecurityException("Invalid or expired JWT token");
        }
        String email = userService.getEmailFromToken(jwt, ip, userAgent);
        if (email == null) {
            throw new SecurityException("Unable to extract email from JWT");
        }
        return email;
    }
}