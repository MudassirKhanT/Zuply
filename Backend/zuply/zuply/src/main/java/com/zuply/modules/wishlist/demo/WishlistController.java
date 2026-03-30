package com.zuply.modules.wishlist.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.wishlist.model.Wishlist;
import com.zuply.modules.wishlist.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<List<Wishlist>>> getWishlist(
            @PathVariable Long customerId) {
        List<Wishlist> wishlist = wishlistService.findByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist fetched"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Wishlist>> addToWishlist(
            @RequestBody Wishlist wishlist) {
        Wishlist saved = wishlistService.save(wishlist);
        return ResponseEntity.ok(ApiResponse.success(saved, "Added to wishlist"));
    }

    @DeleteMapping("/{customerId}/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long customerId,
            @PathVariable Long productId) {
        wishlistService.removeByCustomerIdAndProductId(customerId, productId);
        return ResponseEntity.ok(ApiResponse.success("Removed", "Removed from wishlist"));
    }
}