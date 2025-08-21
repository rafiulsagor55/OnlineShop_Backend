package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "latest") String sort,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        try {
            List<Review> reviews = reviewService.getReviewsForProduct(productId, sort, jwt, ip, userAgent);
            return ResponseEntity.ok(reviews);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/ratings")
    public ResponseEntity<List<Rating>> getRatings(
            @PathVariable String productId,
            HttpServletRequest request) {
        try {
            List<Rating> ratings = reviewService.getRatingsForProduct(productId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/reviews")
    public ResponseEntity<Review> addReview(
            @PathVariable String productId,
            @RequestBody Map<String, String> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String reviewText = body.get("reviewText");
        try {
            Review review = reviewService.addReview(productId, reviewText, jwt, ip, userAgent);
            return ResponseEntity.ok(review);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/ratings")
    public ResponseEntity<Rating> addRating(
            @PathVariable String productId,
            @RequestBody Map<String, String> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String ratingStr = body.get("rating");
        if (ratingStr == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            int rating = Integer.parseInt(ratingStr);
            Rating newRating = reviewService.addRating(productId, rating, jwt, ip, userAgent);
            return ResponseEntity.ok(newRating);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(null);
        } catch (SecurityException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @PutMapping("/ratings")
    public ResponseEntity<Void> updateRating(
            @PathVariable String productId,
            @RequestBody Map<String, String> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String ratingStr = body.get("rating");
        if (ratingStr == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            int rating = Integer.parseInt(ratingStr);
            boolean updated = reviewService.updateRating(productId, rating, jwt, ip, userAgent);
            return updated ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @DeleteMapping("/ratings")
    public ResponseEntity<Void> deleteRating(
            @PathVariable String productId,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        try {
            boolean deleted = reviewService.deleteRating(productId, jwt, ip, userAgent);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String productId,
            @PathVariable Long reviewId,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        try {
            boolean deleted = reviewService.deleteReview(reviewId, productId, jwt, ip, userAgent);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/reviews/vote")
    public ResponseEntity<Void> vote(
            @PathVariable String productId,
            @RequestBody Map<String, Object> body,
            @CookieValue(name = "token", required = false) String jwt,
            HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null) ip = request.getRemoteAddr();
        String voteType = (String) body.get("voteType");
        Long reviewId = Long.valueOf(body.get("reviewId").toString());
        try {
            reviewService.vote(reviewId, voteType, jwt, ip, userAgent);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(401).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}