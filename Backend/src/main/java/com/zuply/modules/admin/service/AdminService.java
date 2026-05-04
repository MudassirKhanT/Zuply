package com.zuply.modules.admin.service;

import com.zuply.common.enums.ProductStatus;
import com.zuply.common.enums.Role;
import com.zuply.exception.ResourceNotFoundException;
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminProductUpdateRequest;
import com.zuply.modules.admin.dto.AdminReportDto;
import com.zuply.modules.admin.dto.SellerAdminDto;
import com.zuply.modules.category.model.Category;
import com.zuply.modules.category.repository.CategoryRepository;
import com.zuply.modules.listing.model.Product;
import com.zuply.modules.listing.repository.ProductRepository;
import com.zuply.modules.order.repository.OrderRepository;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final SellerRepository sellerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    @Qualifier("listingProductRepository")
    private ProductRepository listingProductRepository;

    @Autowired
    private com.zuply.modules.product.repository.ProductRepository productRepository;

    @Autowired
    public AdminService(SellerRepository sellerRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository,
                        CategoryRepository categoryRepository) {
        this.sellerRepository = sellerRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    public AdminDashboardDto getDashboard() {
        long totalSellers  = sellerRepository.count();
        long totalProducts = listingProductRepository.count();
        long totalOrders   = orderRepository.count();
        return new AdminDashboardDto(totalSellers, totalProducts, totalOrders);
    }

    // ─── Seller Management ───────────────────────────────────────────────────

    public List<SellerAdminDto> getAllSellers() {
        return sellerRepository.findAll().stream()
                .map(this::toSellerAdminDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SellerAdminDto approveSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setVerificationStatus("APPROVED");
        seller.setActive(true);
        return toSellerAdminDto(sellerRepository.save(seller));
    }

    @Transactional
    public SellerAdminDto suspendSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setActive(false);
        seller.setVerificationStatus("SUSPENDED");
        return toSellerAdminDto(sellerRepository.save(seller));
    }

    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        sellerRepository.delete(seller);
    }

    // ─── Product Management ──────────────────────────────────────────────────

    public List<Product> getAllProducts() {
        return listingProductRepository.findAll();
    }

    @Transactional
    public Product approveProduct(Long id) {
        Product listing = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // listing.getSellerId() stores the USER's id, not the seller's primary key
        Seller seller = sellerRepository.findByUserId(listing.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found for product id: " + id));

        // Find or create the matching category
        Category category = null;
        if (listing.getCategory() != null && !listing.getCategory().isBlank()) {
            category = categoryRepository.findByNameIgnoreCase(listing.getCategory())
                    .orElseGet(() -> {
                        Category c = new Category();
                        c.setName(listing.getCategory());
                        c.setSlug(listing.getCategory().toLowerCase().replace(" ", "-"));
                        return categoryRepository.save(c);
                    });
        }

        // Bridge: copy the listing into the customer-visible products table
        com.zuply.modules.product.model.Product market = new com.zuply.modules.product.model.Product();
        market.setName(listing.getTitle());
        market.setDescription(listing.getDescription());
        market.setSeller(seller);
        market.setCategory(category);
        market.setPrice(listing.getPrice());
        market.setStock(50);
        market.setImageUrl(listing.getImageUrl());
        market.setStatus(ProductStatus.APPROVED);
        productRepository.save(market);

        listing.setStatus("APPROVED");
        return listingProductRepository.save(listing);
    }

    @Transactional
    public Product rejectProduct(Long id) {
        Product listing = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        listing.setStatus("REJECTED");
        return listingProductRepository.save(listing);
    }

    @Transactional
    public Product updateProduct(Long id, AdminProductUpdateRequest request) {
        Product product = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (request.getTitle()       != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory()    != null) product.setCategory(request.getCategory());
        if (request.getPrice()       != null) product.setPrice(request.getPrice());
        if (request.getStatus()      != null) product.setStatus(request.getStatus());
        return listingProductRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!listingProductRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        listingProductRepository.deleteById(id);
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    public AdminReportDto getReports() {
        BigDecimal totalSales = orderRepository.sumAllOrderAmounts();
        if (totalSales == null) totalSales = BigDecimal.ZERO;

        long totalSellers   = sellerRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);

        Map<String, Long> productsByCategory = listingProductRepository.findAll().stream()
                .filter(p -> p.getCategory() != null)
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));

        return new AdminReportDto(totalSales, totalSellers, totalCustomers, productsByCategory);
    }

    // ─── One-time Migration ──────────────────────────────────────────────────

    @Transactional
    public int backfillSellerRecords() {
        java.util.List<com.zuply.modules.user.model.User> sellerUsers =
                userRepository.findByRole(Role.SELLER);
        int created = 0;
        for (com.zuply.modules.user.model.User user : sellerUsers) {
            boolean exists = sellerRepository.findByUserId(user.getId()).isPresent();
            if (!exists) {
                Seller seller = new Seller();
                seller.setUser(user);
                seller.setStoreName(user.getName() + "'s Store");
                seller.setVerificationStatus("PENDING");
                seller.setActive(false);
                sellerRepository.save(seller);
                created++;
            }
        }
        return created;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private SellerAdminDto toSellerAdminDto(Seller seller) {
        return SellerAdminDto.builder()
                .id(seller.getId())
                .name(seller.getUser() != null ? seller.getUser().getName() : "N/A")
                .email(seller.getUser() != null ? seller.getUser().getEmail() : "N/A")
                .phone(seller.getUser() != null ? seller.getUser().getPhone() : "N/A")
                .storeName(seller.getStoreName())
                .location(seller.getLocation())
                .pincode(seller.getPincode())
                .verificationStatus(seller.getVerificationStatus())
                .active(seller.isActive())
                .build();
    }
}
