package com.zuply.modules.seller.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.listing.model.Product;
import com.zuply.modules.order.model.Order;
import com.zuply.modules.seller.dto.SellerDashboardDto;
import com.zuply.modules.seller.dto.SellerOrderDto;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.service.SellerService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private UserService userService;

    private Seller getAuthenticatedSeller(Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sellerService.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Seller record not found. Please register as seller first."));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Seller>> registerSeller(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        String email = authentication.getName();
        User user = userService.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(404).body(ApiResponse.failure("User not found"));
        }

        Optional<Seller> existingSeller = sellerService.findByUserId(user.getId());
        if (existingSeller.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Seller record already exists for this user"));
        }

        String storeName = (String) request.get("storeName");
        String location  = (String) request.get("location");
        String pincode   = (String) request.get("pincode");

        Seller seller = sellerService.registerSeller(user, storeName, location, pincode);
        return ResponseEntity.ok(ApiResponse.success("Seller registered successfully. Awaiting admin approval.", seller));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<SellerDashboardDto>> getDashboard(Authentication authentication) {
        Seller seller;
        try {
            seller = getAuthenticatedSeller(authentication);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }

        if (!seller.isActive()) {
            return ResponseEntity.status(403).body(ApiResponse.failure("Account suspended"));
        }

        SellerDashboardDto dashboard = sellerService.getDashboard(seller.getId());
        return ResponseEntity.ok(ApiResponse.success("Dashboard fetched successfully", dashboard));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getSellerProducts(Authentication authentication) {
        Seller seller;
        try {
            seller = getAuthenticatedSeller(authentication);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }

        if (!seller.isActive()) {
            return ResponseEntity.status(403).body(ApiResponse.failure("Account suspended"));
        }

        List<Product> products = sellerService.getSellerProducts(seller.getId());
        return ResponseEntity.ok(ApiResponse.success("Seller products fetched", products));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<SellerOrderDto>>> getSellerOrders(Authentication authentication) {
        Seller seller;
        try {
            seller = getAuthenticatedSeller(authentication);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }

        if (!seller.isActive()) {
            return ResponseEntity.status(403).body(ApiResponse.failure("Account suspended"));
        }

        List<SellerOrderDto> orders = sellerService.getSellerOrders(seller.getId());
        return ResponseEntity.ok(ApiResponse.success("Seller orders fetched", orders));
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            Authentication authentication) {
        Seller seller;
        try {
            seller = getAuthenticatedSeller(authentication);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }

        if (!seller.isActive()) {
            return ResponseEntity.status(403).body(ApiResponse.failure("Account suspended"));
        }

        try {
            Order updatedOrder = sellerService.updateOrderStatus(seller.getId(), id, status);
            return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.failure(e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> getSellerById(@PathVariable Long id) {
        Optional<Seller> seller = sellerService.findById(id);
        return seller.map(s -> ResponseEntity.ok(ApiResponse.success("Seller found", s)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Seller not found")));
    }

    /** Public endpoint — returns basic info for active/approved sellers (no auth required). */
    @GetMapping("/public")
    public ResponseEntity<ApiResponse<java.util.List<java.util.Map<String, Object>>>> getPublicSellers() {
        java.util.List<java.util.Map<String, Object>> result = sellerService.findAll().stream()
                .filter(s -> s.isActive() && "APPROVED".equals(s.getVerificationStatus()))
                .map(s -> {
                    java.util.Map<String, Object> m = new java.util.HashMap<>();
                    m.put("id",        s.getId());
                    m.put("storeName", s.getStoreName());
                    m.put("location",  s.getLocation());
                    m.put("pincode",   s.getPincode());
                    m.put("name",      s.getUser() != null ? s.getUser().getName() : "");
                    return m;
                })
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Sellers fetched", result));
    }
}
