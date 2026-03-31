package com.zuply.modules.product.repository;

import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all products by status
    List<Product> findByStatus(ProductStatus status);

    // Search by name and status
    List<Product> findByNameContainingAndStatus(
            String name, ProductStatus status);

    // Find by seller
    List<Product> findBySellerId(Long sellerId);

    // Find by seller pincode and status
    List<Product> findBySellerPincodeAndStatus(
            String pincode, ProductStatus status);
}