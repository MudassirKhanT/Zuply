package com.zuply.modules.listing.repository;

import com.zuply.modules.listing.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository("listingProductRepository")
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByImageId(Long imageId);
    Optional<Product> findByIdAndSellerId(Long id, Long sellerId);
    List<Product> findBySellerId(Long sellerId);
    List<Product> findBySellerIdOrderByIdDesc(Long sellerId);
}
