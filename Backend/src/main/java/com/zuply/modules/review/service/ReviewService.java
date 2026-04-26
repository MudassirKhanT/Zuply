package com.zuply.modules.review.service;

import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.review.dto.ReviewDto;
import com.zuply.modules.review.dto.ReviewRequest;
import com.zuply.modules.review.model.Review;
import com.zuply.modules.review.repository.ReviewRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    public List<ReviewDto> getReviews(Long productId) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public Double getAverageRating(Long productId) {
        Double avg = reviewRepository.avgRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }

    public ReviewDto addReview(Long productId, ReviewRequest request, String customerEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        User customer = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (reviewRepository.existsByProductIdAndCustomerId(productId, customer.getId())) {
            throw new RuntimeException("You have already reviewed this product");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setCustomer(customer);
        review.setRating(request.getRating());
        review.setComment(request.getComment());

        return toDto(reviewRepository.save(review));
    }

    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setCustomerName(review.getCustomer().getName());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt() != null
                ? review.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : null);
        return dto;
    }
}
