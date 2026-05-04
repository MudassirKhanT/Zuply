package com.zuply.modules.admin.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.dto.CreateAdminRequest;
import com.zuply.modules.admin.service.AdminService;
import com.zuply.modules.admin.dto.AdminSellerDto;
import com.zuply.modules.product.dto.ProductDto;
import com.zuply.modules.seller.model.Seller;
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

    // ─── Admin Account Creation ───────────────────────────────────────────────

    @PostMapping("/create-admin")
    public ResponseEntity<ApiResponse<String>> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        adminService.createAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Admin account created for " + request.getEmail(), "Admin created successfully"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardDto>> getDashboard() {
        AdminDashboardDto dto = adminService.getDashboard();
        return ResponseEntity.ok(
                ApiResponse.success(dto, "Dashboard data fetched successfully"));
    }

    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<AdminSellerDto>>> getAllSellers() {
        List<AdminSellerDto> dtos = adminService.getAllSellers().stream()
                .map(AdminSellerDto::new).toList();
        return ResponseEntity.ok(ApiResponse.success(dtos, "Sellers fetched successfully"));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<AdminSellerDto>> approveSeller(@PathVariable Long id) {
        Seller seller = adminService.approveSeller(id);
        return ResponseEntity.ok(ApiResponse.success(new AdminSellerDto(seller), "Seller approved successfully"));
    }

    @PatchMapping("/sellers/{id}/suspend")
    public ResponseEntity<ApiResponse<AdminSellerDto>> suspendSeller(@PathVariable Long id) {
        Seller seller = adminService.suspendSeller(id);
        return ResponseEntity.ok(ApiResponse.success(new AdminSellerDto(seller), "Seller suspended successfully"));
    }

    @PatchMapping("/sellers/{id}/reject")
    public ResponseEntity<ApiResponse<AdminSellerDto>> rejectSeller(@PathVariable Long id) {
        Seller seller = adminService.rejectSeller(id);
        return ResponseEntity.ok(ApiResponse.success(new AdminSellerDto(seller), "Seller rejected successfully"));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.getAllProducts(), "Products fetched successfully"));
    }

    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<ProductDto>> approveProduct(@PathVariable Long id) {
        try {
            ProductDto product = adminService.approveProduct(id);
            return ResponseEntity.ok(ApiResponse.success(product, "Product approved successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<ProductDto>> rejectProduct(@PathVariable Long id) {
        try {
            ProductDto product = adminService.rejectProduct(id);
            return ResponseEntity.ok(ApiResponse.success(product, "Product rejected successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportDto>> getReports() {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.getReports(), "Reports fetched successfully"));
    }
}
