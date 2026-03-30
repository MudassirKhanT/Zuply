package com.zuply.modules.order.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.order.dto.CheckoutRequest;
import com.zuply.modules.order.dto.OrderDto;
import com.zuply.modules.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // POST /api/orders — place order from cart
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDto>> placeOrder(
            @Valid @RequestBody CheckoutRequest request) {
        try {
            OrderDto order = orderService.placeOrder(request);
            return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.error(e.getMessage()));
        }
    }

    // GET /api/orders?customerId={id} — customer order history
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderDto>>> getOrders(
            @RequestParam Long customerId) {
        List<OrderDto> orders = orderService.getOrdersByCustomer(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders fetched"));
    }

    // GET /api/orders/{id} — single order detail
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderDto>> getOrderById(@PathVariable Long id) {
        try {
            OrderDto order = orderService.getOrderById(id);
            return ResponseEntity.ok(ApiResponse.success(order, "Order found"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }
}