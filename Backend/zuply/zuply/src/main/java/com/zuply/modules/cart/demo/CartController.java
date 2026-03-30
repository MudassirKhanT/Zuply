package com.zuply.modules.cart.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.cart.model.Cart;
import com.zuply.modules.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<ApiResponse<Cart>> getCart(@PathVariable Long customerId) {
        Optional<Cart> cart = cartService.findByCustomerId(customerId);
        return cart.map(c -> ResponseEntity.ok(ApiResponse.success(c, "Cart fetched")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.error("Cart not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Cart>> saveCart(@RequestBody Cart cart) {
        Cart saved = cartService.save(cart);
        return ResponseEntity.ok(ApiResponse.success(saved, "Cart saved"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteCart(@PathVariable Long id) {
        cartService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", "Cart deleted"));
    }
}