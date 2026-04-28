package com.zuply.modules.admin.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminProductUpdateRequest;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.dto.SellerAdminDto;
import com.zuply.modules.admin.service.AdminService;
import com.zuply.modules.listing.model.Product;
import jakarta.validation.Valid;
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
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard data fetched successfully", adminService.getDashboard()));
    }

    // ─── Seller Management ───────────────────────────────────────────────────

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerAdminDto>>> getAllSellers() {
        return ResponseEntity.ok(
                ApiResponse.success("Sellers fetched successfully", adminService.getAllSellers()));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<SellerAdminDto>> approveSeller(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Seller approved successfully", adminService.approveSeller(id)));
    }

    @PatchMapping("/sellers/{id}/suspend")
    public ResponseEntity<ApiResponse<SellerAdminDto>> suspendSeller(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Seller suspended successfully", adminService.suspendSeller(id)));
    }

    @DeleteMapping("/sellers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSeller(@PathVariable Long id) {
        adminService.deleteSeller(id);
        return ResponseEntity.ok(ApiResponse.success("Seller deleted successfully", null));
    }

    // ─── Product Management ──────────────────────────────────────────────────

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.success("Products fetched successfully", adminService.getAllProducts()));
    }

    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product approved and published to marketplace", adminService.approveProduct(id)));
    }

    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Product rejected", adminService.rejectProduct(id)));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody AdminProductUpdateRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Product updated successfully", adminService.updateProduct(id, request)));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        adminService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    // ─── Migration ───────────────────────────────────────────────────────────

    @PostMapping("/migrate/backfill-sellers")
    public ResponseEntity<ApiResponse<String>> backfillSellers() {
        int created = adminService.backfillSellerRecords();
        return ResponseEntity.ok(ApiResponse.success(
                "Backfill complete. Seller records created: " + created,
                created + " seller record(s) created"));
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportDto>> getReports() {
        return ResponseEntity.ok(
                ApiResponse.success("Reports fetched successfully", adminService.getReports()));
    }
}
