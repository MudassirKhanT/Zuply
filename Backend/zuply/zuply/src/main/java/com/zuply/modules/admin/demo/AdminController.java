package com.zuply.modules.admin.demo;

import com.zuply.common.ApiResponse;   // FIXED: was com.zuply.payload.ApiResponse
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.service.AdminService;
import com.zuply.modules.admin.dto.AdminSellerDto;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.seller.model.Seller;
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

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.getAllProducts(), "Products fetched successfully"));
    }

    @PatchMapping("/products/{id}/approve")
    public ResponseEntity<ApiResponse<Product>> approveProduct(@PathVariable Long id) {
        Product product = adminService.approveProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success(product, "Product approved successfully"));
    }

    @PatchMapping("/products/{id}/reject")
    public ResponseEntity<ApiResponse<Product>> rejectProduct(@PathVariable Long id) {
        Product product = adminService.rejectProduct(id);
        return ResponseEntity.ok(
                ApiResponse.success(product, "Product rejected successfully"));
    }

    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<AdminReportDto>> getReports() {
        return ResponseEntity.ok(
                ApiResponse.success(adminService.getReports(), "Reports fetched successfully"));
    }
}
