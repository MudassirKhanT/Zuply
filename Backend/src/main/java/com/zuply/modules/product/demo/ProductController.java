package com.zuply.modules.product.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.product.dto.ProductDto;
import com.zuply.modules.product.dto.ProductRequest;
import com.zuply.modules.product.service.ProductService;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired private ProductService productService;
    @Autowired private UserRepository userRepository;
    @Autowired private SellerRepository sellerRepository;

    // ── Helper: resolve Seller from JWT ──────────────────────────────────────

    private Seller getSellerFromAuth(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return sellerRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Seller s = new Seller();
                    s.setUser(user);
                    s.setStoreName(user.getName() + "'s Store");
                    s.setVerificationStatus("PENDING");
                    s.setActive(false);
                    return sellerRepository.save(s);
                });
    }

    // ── Public: Search, Filter, Sort ─────────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String sortBy) {

        List<ProductDto> products = productService.searchProducts(name, pincode, sortBy);
        if (products.isEmpty()) {
            String emptyMsg = (pincode != null && !pincode.isEmpty())
                    ? "No products available in this location"
                    : "No products available";
            return ResponseEntity.ok(ApiResponse.success(emptyMsg, products));
        }
        return ResponseEntity.ok(ApiResponse.success("Products fetched", products));
    }

    // ── Public: Single Product ────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        Optional<ProductDto> product = productService.findById(id);
        return product
                .map(p -> ResponseEntity.ok(ApiResponse.success("Product found", p)))
                .orElse(ResponseEntity.status(404).body(ApiResponse.failure("Product not found")));
    }

    // ── Seller: Create Product ────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @RequestBody ProductRequest request,
            Authentication authentication) {   // ADDED: Authentication parameter
        try {
            // FIXED: sellerId is resolved from JWT, not from request body
            Seller seller = getSellerFromAuth(authentication);
            ProductDto created = productService.createProduct(request, seller.getId());
            return ResponseEntity.ok(ApiResponse.success("Product created successfully", created));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.failure(e.getMessage()));
        }
    }

    // ── Seller: Update Product ────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request,
            Authentication authentication) {   // FIXED: was @RequestParam Long sellerId
        try {
            Seller seller = getSellerFromAuth(authentication);
            ProductDto updated = productService.updateProduct(id, request, seller.getId());
            return ResponseEntity.ok(ApiResponse.success("Product updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.failure(e.getMessage()));
        }
    }

    // ── Seller: Delete Product ────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication) {   // FIXED: was @RequestParam Long sellerId
        try {
            Seller seller = getSellerFromAuth(authentication);
            productService.deleteProduct(id, seller.getId());
            return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", "Deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(ApiResponse.failure(e.getMessage()));
        }
    }

    // ── Seller: Get Own Products ──────────────────────────────────────────────

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getSellerProducts(
            @PathVariable Long sellerId) {
        List<ProductDto> products = productService.findBySellerId(sellerId);
        return ResponseEntity.ok(ApiResponse.success("Seller products fetched", products));
    }
}
