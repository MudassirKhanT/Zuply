package com.zuply.modules.product.repository;

import com.zuply.modules.product.model.Product;
import com.zuply.common.enums.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByNameContainingAndStatus(String name, ProductStatus status);
    List<Product> findBySellerId(Long sellerId);
}