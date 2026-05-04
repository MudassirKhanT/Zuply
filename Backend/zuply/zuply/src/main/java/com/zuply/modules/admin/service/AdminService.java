package com.zuply.modules.admin.service;

import com.zuply.common.enums.ProductStatus;
import com.zuply.common.enums.Role;
import com.zuply.exception.ResourceNotFoundException;
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.dto.CreateAdminRequest;
import com.zuply.modules.order.repository.OrderRepository;
import com.zuply.modules.product.dto.ProductDto;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.product.service.ProductService;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── Admin Account Creation ──────────────────────────────────────────────

    @Transactional
    public User createAdmin(CreateAdminRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    public AdminDashboardDto getDashboard() {
        long totalSellers  = sellerRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders   = orderRepository.count();
        return new AdminDashboardDto(totalSellers, totalProducts, totalOrders);
    }

    // ─── Seller Management ───────────────────────────────────────────────────

    public java.util.List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    @Transactional
    public Seller approveSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setVerificationStatus("APPROVED");
        seller.setActive(true);
        return sellerRepository.save(seller);
    }

    @Transactional
    public Seller suspendSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setActive(false);
        return sellerRepository.save(seller);
    }

    @Transactional
    public Seller rejectSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setVerificationStatus("REJECTED");
        seller.setActive(false);
        return sellerRepository.save(seller);
    }

    // ─── Product Management ──────────────────────────────────────────────────

    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(p -> productService.toPublicDto(p))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDto approveProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setStatus(ProductStatus.APPROVED);
        return productService.toPublicDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto rejectProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        product.setStatus(ProductStatus.REJECTED);
        return productService.toPublicDto(productRepository.save(product));
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    public AdminReportDto getReports() {
        BigDecimal totalSales = orderRepository.sumAllOrderAmounts();
        if (totalSales == null) totalSales = BigDecimal.ZERO;

        long totalSellers   = sellerRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        Map<String, Long> productsByCategory = productRepository.findAll().stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(
                        p -> p.getCategory().getName(),
                        Collectors.counting()
                ));

        return new AdminReportDto(totalSales, totalSellers, totalCustomers, productsByCategory);
    }
}
