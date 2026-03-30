package com.zuply.modules.admin.service;

import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.order.repository.OrderRepository;
import com.zuply.common.enums.ProductStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Seller> findAllSellers() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> findSellerById(Long id) {
        return sellerRepository.findById(id);
    }

    public Seller saveSeller(Seller seller) {
        return sellerRepository.save(seller);
    }

    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public long countSellers() {
        return sellerRepository.count();
    }

    public long countProducts() {
        return productRepository.count();
    }

    public long countOrders() {
        return orderRepository.count();
    }
}