package com.zuply.modules.wishlist.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.wishlist.dto.WishlistItemDto;
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

    // GET /api/wishlist?customerId={id}  — get full wishlist
    @GetMapping
    public ResponseEntity<ApiResponse<List<WishlistItemDto>>> getWishlist(
            @RequestParam Long customerId) {
        List<WishlistItemDto> wishlist = wishlistService.getWishlistByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(wishlist, "Wishlist fetched"));
    }

    // POST /api/wishlist/{productId}?customerId={id}  — add to wishlist
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishlistItemDto>> addToWishlist(
            @PathVariable Long productId,
            @RequestParam Long customerId) {
        try {
            WishlistItemDto item = wishlistService.addToWishlist(customerId, productId);
            return ResponseEntity.ok(ApiResponse.success(item, "Added to wishlist"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /api/wishlist/{productId}?customerId={id}  — remove from wishlist
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<String>> removeFromWishlist(
            @PathVariable Long productId,
            @RequestParam Long customerId) {
        try {
            wishlistService.removeFromWishlist(customerId, productId);
            return ResponseEntity.ok(ApiResponse.success("Removed", "Removed from wishlist"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}