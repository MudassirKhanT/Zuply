package com.zuply.modules.product.service;

import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> findAllApproved() {
        return productRepository.findByStatus(ProductStatus.APPROVED);
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContainingAndStatus(
                name, ProductStatus.APPROVED);
    }

    public List<Product> findBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}