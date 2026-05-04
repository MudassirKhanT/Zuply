package com.zuply.modules.review.service;

import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.review.dto.ReviewDto;
import com.zuply.modules.review.dto.ReviewRequest;
import com.zuply.modules.review.model.Review;
import com.zuply.modules.review.repository.ReviewRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public List<ReviewDto> getReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(ReviewDto::new).collect(Collectors.toList());
    }

    @Transactional
    public ReviewDto submitReview(Long productId, Long userId, ReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = reviewRepository.findByProductIdAndUserId(productId, userId)
                .orElse(new Review());

        review.setProduct(product);
        review.setUser(user);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return new ReviewDto(reviewRepository.save(review));
    }

    public double getAverageRating(Long productId) {
        Double avg = reviewRepository.averageRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public long getReviewCount(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}
