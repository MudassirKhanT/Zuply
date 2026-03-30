package com.zuply.modules.admin.demo;

import com.zuply.common.ApiResponse;
import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.admin.service.AdminService;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.seller.model.Seller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboard() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalSellers", adminService.countSellers());
        stats.put("totalProducts", adminService.countProducts());
        stats.put("totalOrders", adminService.countOrders());
        return ResponseEntity.ok(ApiResponse.success(stats, "Dashboard fetched"));
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<Seller>>> getAllSellers() {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.findAllSellers(), "Sellers fetched"));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<Seller>> approveSeller(@PathVariable Long id) {
        Optional<Seller> seller = adminService.findSellerById(id);
        if (seller.isPresent()) {
            seller.get().setVerificationStatus("APPROVED");
            return ResponseEntity.ok(ApiResponse.success(
                    adminService.saveSeller(seller.get()), "Seller approved"));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Seller not found"));
    }

    @PatchMapping("/sellers/{id}/suspend")
    public ResponseEntity<ApiResponse<Seller>> suspendSeller(@PathVariable Long id) {
        Optional<Seller> seller = adminService.findSellerById(id);
        if (seller.isPresent()) {
            seller.get().setActive(false);
            return ResponseEntity.ok(ApiResponse.success(
                    adminService.saveSeller(seller.get()), "Seller suspended"));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Seller not found"));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(
                adminService.findAllProducts(), "Products fetched"));
    }

    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable Long id) {
        Optional<Product> product = adminService.findProductById(id);
        if (product.isPresent()) {
            product.get().setStatus(ProductStatus.APPROVED);
            return ResponseEntity.ok(ApiResponse.success(
                    adminService.saveProduct(product.get()), "Product approved"));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Product not found"));
    }

    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable Long id) {
        Optional<Product> product = adminService.findProductById(id);
        if (product.isPresent()) {
            product.get().setStatus(ProductStatus.REJECTED);
            return ResponseEntity.ok(ApiResponse.success(
                    adminService.saveProduct(product.get()), "Product rejected"));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Product not found"));
    }
}
