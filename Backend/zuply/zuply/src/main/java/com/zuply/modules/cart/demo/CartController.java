package com.zuply.modules.cart.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.cart.dto.CartDto;
import com.zuply.modules.cart.dto.CartItemRequest;
import com.zuply.modules.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/cart?customerId={id}  — get cart with all items and grand total
    @GetMapping
    public ResponseEntity<ApiResponse<CartDto>> getCart(@RequestParam Long customerId) {
        try {
            CartDto cart = cartService.getCartByCustomerId(customerId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart fetched"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    // POST /api/cart?customerId={id}  — add item to cart
    @PostMapping
    public ResponseEntity<ApiResponse<CartDto>> addToCart(
            @RequestParam Long customerId,
            @RequestBody CartItemRequest request) {
        try {
            CartDto cart = cartService.addItemToCart(customerId, request);
            return ResponseEntity.ok(ApiResponse.success(cart, "Item added to cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }

    // PUT /api/cart/{itemId}?customerId={id}&quantity={qty}  — update quantity
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> updateQuantity(
            @PathVariable Long itemId,
            @RequestParam Long customerId,
            @RequestParam Integer quantity) {
        try {
            CartDto cart = cartService.updateCartItemQuantity(customerId, itemId, quantity);
            return ResponseEntity.ok(ApiResponse.success(cart, "Cart updated"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    // DELETE /api/cart/{itemId}?customerId={id}  — remove single item
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<CartDto>> removeItem(
            @PathVariable Long itemId,
            @RequestParam Long customerId) {
        try {
            CartDto cart = cartService.removeItemFromCart(customerId, itemId);
            return ResponseEntity.ok(ApiResponse.success(cart, "Item removed from cart"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}