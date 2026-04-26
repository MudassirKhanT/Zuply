package com.zuply.modules.order.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.order.dto.CheckoutRequest;
import com.zuply.modules.order.dto.OrderDto;
import com.zuply.modules.order.service.OrderService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private UserRepository userRepository;

    private User getAuthUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // POST /api/orders — place order from cart
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            request.setCustomerId(user.getId());
            OrderDto order = orderService.placeOrder(request);
            return ResponseEntity.ok(ApiResponse.success("Order placed successfully", order));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    // GET /api/orders — get current user's orders
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getMyOrders(
            Authentication authentication) {
        User user = getAuthUser(authentication);
        List<OrderDto> orders = orderService.findByCustomerId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orders));
    }

    // GET /api/orders/{id} — single order detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(
            @PathVariable Long id,
            Authentication authentication) {
        try {
            User user = getAuthUser(authentication);
            OrderDto order = orderService.findByIdAndCustomerId(id, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Order found", order));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }
    }
}
