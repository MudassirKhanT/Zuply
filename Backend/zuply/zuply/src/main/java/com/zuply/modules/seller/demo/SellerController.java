package com.zuply.modules.seller.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.service.SellerService;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @Autowired
    private ProductService productService;

    @GetMapping("/{sellerId}/products")
    public ResponseEntity<ApiResponse<List<Product>>> getSellerProducts(
            @PathVariable Long sellerId) {
        List<Product> products = productService.findBySeller(sellerId);
        return ResponseEntity.ok(ApiResponse.success(products, "Seller products fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Seller>> getSellerById(@PathVariable Long id) {
        Optional<Seller> seller = sellerService.findById(id);
        return seller.map(s -> ResponseEntity.ok(ApiResponse.success(s, "Seller found")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.error("Seller not found")));
    }
}