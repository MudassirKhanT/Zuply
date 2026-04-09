package com.zuply.modules.cart.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.cart.dto.CartDto;
import com.zuply.modules.cart.dto.CartItemRequest;
import com.zuply.modules.cart.service.CartService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired private CartService cartService;
    @Autowired private UserRepository userRepository;

    private User getAuthUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // GET /api/cart  ── returns cart items + grand total
    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            CartDto cart = cartService.getCart(user.getId());
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart fetched"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/cart  ── add item (or increase qty if product already in cart)
    @PostMapping
    public ResponseEntity<ApiResponse<CartDto>> addItem(
            @Valid @RequestBody CartItemRequest request,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            CartDto cart = cartService.addItem(user.getId(), request);
            return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // PUT /api/cart/{itemId}  ── update quantity (0 = remove)
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateItem(
            @PathVariable Long itemId,
            @RequestParam int quantity,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            CartDto cart = cartService.updateItemQuantity(user.getId(), itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /api/cart/{itemId}  ── remove single item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            CartDto cart = cartService.removeItem(user.getId(), itemId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
