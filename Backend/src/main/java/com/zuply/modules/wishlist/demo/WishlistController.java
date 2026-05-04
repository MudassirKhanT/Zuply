package com.zuply.modules.wishlist.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import com.zuply.modules.wishlist.model.Wishlist;
import com.zuply.modules.wishlist.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired private WishlistService wishlistService;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;

    // ── Helper ────────────────────────────────────────────────────────────────

    private User getAuthUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── GET /api/wishlist ─────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(
            Authentication authentication) {
        User user = getAuthUser(authentication);
        List<Wishlist> wishlist = wishlistService.findByCustomerId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Wishlist fetched", wishlist));
    }

    // ── POST /api/wishlist/{productId} ────────────────────────────────────────

    @PostMapping("/{productId}")   // FIXED: was @PostMapping with body=Wishlist
    public ResponseEntity<ApiResponse<Wishlist>> addToWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);

            if (wishlistService.existsByCustomerIdAndProductId(user.getId(), productId)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.failure("Product already in wishlist"));
            }

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Wishlist wishlist = new Wishlist();
            wishlist.setCustomer(user);
            wishlist.setProduct(product);

            Wishlist saved = wishlistService.save(wishlist);
            return ResponseEntity.ok(ApiResponse.success("Added to wishlist", saved));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    // ── DELETE /api/wishlist/{productId} ──────────────────────────────────────

    @DeleteMapping("/{productId}")   // FIXED: was /{customerId}/{productId}
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long productId,
            Authentication authentication) {
        User user = getAuthUser(authentication);
        wishlistService.removeByCustomerIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(ApiResponse.success("Removed from wishlist", "Removed"));
    }
}
