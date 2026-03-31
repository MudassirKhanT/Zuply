package com.zuply.modules.product.service;

import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.category.model.Category;
import com.zuply.modules.category.repository.CategoryRepository;
import com.zuply.modules.product.dto.ProductDto;
import com.zuply.modules.product.dto.ProductRequest;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SellerRepository sellerRepository;

    // ── Convert Product entity to ProductDto ──────────────────────────
    private ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setStock(product.getStock());
        dto.setVariations(product.getVariations());
        dto.setDeliveryMethod(product.getDeliveryMethod());
        dto.setReturnPolicy(product.getReturnPolicy());
        dto.setImageUrl(product.getImageUrl());
        dto.setStatus(product.getStatus().name());

        if (product.getCategory() != null) {
            dto.setCategoryName(product.getCategory().getName());
        }
        if (product.getSeller() != null) {
            dto.setSellerName(product.getSeller().getStoreName());
            dto.setSellerPincode(product.getSeller().getPincode());
        }
        return dto;
    }

    // ── Search + Filter + Sort ────────────────────────────────────────
    public List<ProductDto> searchProducts(
            String name, String pincode, String sortBy) {

        List<Product> products;

        // Filter by pincode
        if (pincode != null && !pincode.isEmpty()) {
            products = productRepository
                    .findBySellerPincodeAndStatus(pincode, ProductStatus.APPROVED);
        } else {
            products = productRepository
                    .findByStatus(ProductStatus.APPROVED);
        }

        // Filter by name
        if (name != null && !name.isEmpty()) {
            String lowerName = name.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(lowerName))
                    .collect(Collectors.toList());
        }

        // Sort
        if (sortBy != null) {
            switch (sortBy) {
                case "price_asc" -> products.sort(
                        Comparator.comparingDouble(Product::getPrice));
                case "price_desc" -> products.sort(
                        Comparator.comparingDouble(Product::getPrice).reversed());
            }
        }

        return products.stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Get single product ────────────────────────────────────────────
    public Optional<ProductDto> findById(Long id) {
        return productRepository.findById(id).map(this::toDto);
    }

    // ── Get seller's own products ─────────────────────────────────────
    public List<ProductDto> findBySellerId(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    // ── Create new product ────────────────────────────────────────────
    public ProductDto createProduct(ProductRequest request) {

        // Validate name
        if (request.getName() == null || request.getName().isBlank()) {
            throw new RuntimeException("Product name must not be blank");
        }

        // Validate price
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        // Validate stock
        if (request.getStock() == null || request.getStock() < 0) {
            throw new RuntimeException("Stock must be 0 or greater");
        }

        // Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Invalid category"));

        // Find seller
        Seller seller = sellerRepository.findById(request.getSellerId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        // Build product
        Product product = new Product();
        product.setName(request.getName());
        product.setCategory(category);
        product.setSeller(seller);
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setVariations(request.getVariations());
        product.setDeliveryMethod(request.getDeliveryMethod());
        product.setReturnPolicy(request.getReturnPolicy());
        product.setImageUrl(request.getImageUrl());
        product.setStatus(ProductStatus.PENDING); // always starts as PENDING

        return toDto(productRepository.save(product));
    }

    // ── Update product ────────────────────────────────────────────────
    public ProductDto updateProduct(Long id, ProductRequest request,
                                    Long sellerId) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check seller owns this product
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "Forbidden: you can only edit your own products");
        }

        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getPrice() != null)
            product.setPrice(request.getPrice());
        if (request.getStock() != null)
            product.setStock(request.getStock());
        if (request.getVariations() != null)
            product.setVariations(request.getVariations());
        if (request.getDeliveryMethod() != null)
            product.setDeliveryMethod(request.getDeliveryMethod());
        if (request.getReturnPolicy() != null)
            product.setReturnPolicy(request.getReturnPolicy());
        if (request.getImageUrl() != null)
            product.setImageUrl(request.getImageUrl());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository
                    .findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Invalid category"));
            product.setCategory(category);
        }

        return toDto(productRepository.save(product));
    }

    // ── Delete product ────────────────────────────────────────────────
    public void deleteProduct(Long id, Long sellerId) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check seller owns this product
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException(
                    "Forbidden: you can only delete your own products");
        }

        productRepository.deleteById(id);
    }
}