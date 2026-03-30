package com.zuply.modules.product.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts(
            @RequestParam(required = false) String name) {
        List<Product> products = (name != null)
                ? productService.searchByName(name)
                : productService.findAllApproved();
        return ResponseEntity.ok(ApiResponse.success(products, "Products fetched"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(p -> ResponseEntity.ok(ApiResponse.success(p, "Product found")))
                .orElse(ResponseEntity.status(404)
                        .body(ApiResponse.error("Product not found")));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(@RequestBody Product product) {
        Product saved = productService.save(product);
        return ResponseEntity.ok(ApiResponse.success(saved, "Product created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {
        Optional<Product> existing = productService.findById(id);
        if (existing.isPresent()) {
            Product product = existing.get();
            product.setName(updatedProduct.getName());
            product.setPrice(updatedProduct.getPrice());
            product.setStock(updatedProduct.getStock());
            product.setVariations(updatedProduct.getVariations());
            product.setDeliveryMethod(updatedProduct.getDeliveryMethod());
            product.setReturnPolicy(updatedProduct.getReturnPolicy());
            return ResponseEntity.ok(ApiResponse.success(
                    productService.save(product), "Product updated"));
        }
        return ResponseEntity.status(404).body(ApiResponse.error("Product not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Deleted", "Product deleted"));
    }
}