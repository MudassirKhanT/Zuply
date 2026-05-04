package com.zuply.modules.review.controller;

import com.zuply.common.ApiResponse;
import com.zuply.modules.review.dto.ReviewDto;
import com.zuply.modules.review.dto.ReviewRequest;
import com.zuply.modules.review.service.ReviewService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import com.zuply.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewDto>>> getReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(
                ApiResponse.success(reviewService.getReviews(productId), "Reviews fetched"));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<ReviewDto>> submitReview(
            @PathVariable Long productId,
            @Valid @RequestBody ReviewRequest request,
            HttpServletRequest httpRequest) {

        User user = resolveUser(httpRequest);
        ReviewDto dto = reviewService.submitReview(productId, user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(dto, "Review submitted successfully"));
    }

    private User resolveUser(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new RuntimeException("Not authenticated");
        }
        String email = jwtUtil.extractUsername(header.substring(7));
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
