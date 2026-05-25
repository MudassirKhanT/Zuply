package com.zuply.modules.review.controller;

import com.zuply.common.ApiResponse;
import com.zuply.modules.review.dto.ReviewDto;
import com.zuply.modules.review.dto.ReviewRequest;
import com.zuply.modules.review.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/{productId}/reviews")
public class ReviewController {

    @Autowired private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReviews(@PathVariable Long productId) {
        List<ReviewDto> reviews = reviewService.getReviews(productId);
        Double avg = reviewService.getAverageRating(productId);
        return ResponseEntity.ok(ApiResponse.success("Reviews fetched",
                Map.of("reviews", reviews, "averageRating", avg, "count", reviews.size())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewDto>> addReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            Authentication authentication) {
        try {
            ReviewDto review = reviewService.addReview(productId, request, authentication.getName());
            return ResponseEntity.ok(ApiResponse.success("Review added successfully", review));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }
}
