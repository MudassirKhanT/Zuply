package com.zuply.modules.admin.demo;

import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.service.AdminService;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.seller.model.Seller;
import com.zuply.payload.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto dto = adminService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success("Dashboard data fetched successfully", dto));
    }

    // ─── Seller Management ───────────────────────────────────────────────────

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<Seller>>> getAllSellers() {
        return ResponseEntity.ok(ApiResponse.success("Sellers fetched successfully", adminService.getAllSellers()));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<Seller>> approveSeller(@PathVariable Long id) {
        Seller seller = adminService.approveSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller approved successfully", seller));
    }

    @PatchMapping("/sellers/{id}/suspend")
    public ResponseEntity<ApiResponse<Seller>> suspendSeller(@PathVariable Long id) {
        Seller seller = adminService.suspendSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller suspended successfully", seller));
    }

    // ─── Product Management ──────────────────────────────────────────────────

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success("Products fetched successfully", adminService.getAllProducts()));
    }

    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable Long id) {
        Product product = adminService.approveProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product approved successfully", product));
    }

    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable Long id) {
        Product product = adminService.rejectProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product rejected successfully", product));
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportDto>> getReports() {
        return ResponseEntity.ok(ApiResponse.success("Reports fetched successfully", adminService.getReports()));
    }
}