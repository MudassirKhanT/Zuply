package com.zuply.modules.order.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.order.model.Order;
import com.zuply.modules.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(@RequestBody Order order) {
        order.setCreatedAt(LocalDateTime.now());
        Order saved = orderService.save(order);
        return ResponseEntity.ok(ApiResponse.success(saved, "Order placed successfully"));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponse<List<Order>>> getOrdersByCustomer(
            @PathVariable Long customerId) {
        List<Order> orders = orderService.findByCustomerId(customerId);
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(@PathVariable Long id) {
        Optional<Order> order = orderService.findById(id);
        return order.map(o -> ResponseEntity.ok(ApiResponse.success(o, "Order found")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.error("Order not found")));
    }
}