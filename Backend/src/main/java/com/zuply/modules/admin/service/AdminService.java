package com.zuply.modules.admin.service;

import com.zuply.common.enums.ProductStatus;
import com.zuply.common.enums.Role;
import com.zuply.exception.ResourceNotFoundException;
import com.zuply.modules.admin.dto.AdminDashboardDto;
import com.zuply.modules.admin.dto.AdminOrderDto;
import com.zuply.modules.admin.dto.AdminProductDto;
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
import java.util.ArrayList;
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
        // Count approved products in the marketplace (products table) PLUS
        // any listing products that are approved but not yet mirrored
        long approvedMarket   = productRepository.countByStatus(ProductStatus.APPROVED);
        long approvedListings = listingProductRepository.findAll().stream()
                .filter(p -> "APPROVED".equals(p.getStatus()))
                .count();
        // Use the higher of the two to avoid showing 0 when one table is empty
        long totalProducts = Math.max(approvedMarket, approvedListings);
        long totalOrders   = orderRepository.count();
        return new AdminDashboardDto(totalSellers, totalProducts, totalOrders);
    }

    // ─── Seller Management ───────────────────────────────────────────────────

    public List<SellerAdminDto> getAllSellers() {
        return sellerRepository.findAll().stream()
                .filter(s -> !"REJECTED".equals(s.getVerificationStatus()))
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
    public SellerAdminDto rejectSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        seller.setVerificationStatus("REJECTED");
        seller.setActive(false);
        // Mark all their products as REJECTED too
        productRepository.findBySellerId(seller.getId())
                .forEach(p -> { p.setStatus(ProductStatus.REJECTED); productRepository.save(p); });
        return toSellerAdminDto(sellerRepository.save(seller));
    }

    @Transactional
    public void deleteSeller(Long id) {
        Seller seller = sellerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + id));
        sellerRepository.delete(seller);
    }

    // ─── Product Management ──────────────────────────────────────────────────

    /**
     * Returns all listing (AI/upload) products PLUS pending manual products.
     * Listing products are always shown (so admin can see approved/rejected history).
     * Manual products are shown only when PENDING to avoid duplicating the approved
     * entries that were already bridged into the marketplace products table.
     */
    public List<AdminProductDto> getAllProducts() {
        List<AdminProductDto> result = new ArrayList<>();

        // 1. All products from the AI / seller-upload listing table
        List<Product> listings = listingProductRepository.findAll();
        listings.stream()
                .map(this::toAdminProductDtoFromListing)
                .forEach(result::add);

        // Collect imageUrls already covered by listing products
        // so we don't show their marketplace mirror copies again
        java.util.Set<String> listingImageUrls = listings.stream()
                .filter(p -> p.getImageUrl() != null && !p.getImageUrl().isBlank())
                .map(Product::getImageUrl)
                .collect(Collectors.toSet());

        // 2. Only truly manual PENDING products (not mirrored from AI listings)
        productRepository.findByStatus(ProductStatus.PENDING).stream()
                .filter(p -> p.getImageUrl() == null || !listingImageUrls.contains(p.getImageUrl()))
                .map(this::toAdminProductDtoFromMarket)
                .forEach(result::add);

        return result;
    }

    /** Convert an AI-listing product into the unified AdminProductDto. */
    private AdminProductDto toAdminProductDtoFromListing(Product listing) {
        String sellerName = "Unknown Seller";
        Long   sellerUserId = listing.getSellerId();
        if (sellerUserId != null) {
            sellerName = sellerRepository.findByUserId(sellerUserId)
                    .map(Seller::getStoreName)
                    .orElse("Unknown Seller");
        }
        return AdminProductDto.builder()
                .id(listing.getId())
                .name(listing.getTitle())
                .description(listing.getDescription())
                .category(listing.getCategory())
                .price(listing.getPrice())
                .stock(null)          // listing table has no stock field
                .status(listing.getStatus() != null ? listing.getStatus() : "PENDING")
                .sellerName(sellerName)
                .sellerUserId(sellerUserId)
                .imageUrl(listing.getImageUrl())
                .source("LISTING")
                .aiGenerated(listing.isAiSuggestedCategory())
                .build();
    }

    /** Convert a marketplace/manual product into the unified AdminProductDto. */
    private AdminProductDto toAdminProductDtoFromMarket(com.zuply.modules.product.model.Product product) {
        String sellerName   = product.getSeller() != null ? product.getSeller().getStoreName() : "Unknown Seller";
        Long   sellerUserId = (product.getSeller() != null && product.getSeller().getUser() != null)
                ? product.getSeller().getUser().getId() : null;
        String categoryName = product.getCategory() != null ? product.getCategory().getName() : null;

        return AdminProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .category(categoryName)
                .price(product.getPrice())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .sellerName(sellerName)
                .sellerUserId(sellerUserId)
                .imageUrl(product.getImageUrl())
                .source("MANUAL")
                .aiGenerated(false)
                .build();
    }

    /**
     * Approve a product.
     * source = "LISTING" → existing bridge logic (copy listing → products table).
     * source = "MANUAL"  → product is already in the products table; just set APPROVED.
     */
    @Transactional
    public AdminProductDto approveProduct(Long id, String source) {
        if ("MANUAL".equals(source)) {
            com.zuply.modules.product.model.Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

            // Same seller approval gate as LISTING products
            Seller seller = product.getSeller();
            if (seller == null || !"APPROVED".equals(seller.getVerificationStatus()) || !seller.isActive())
                throw new RuntimeException("Cannot approve product: seller account is not yet approved. Approve the seller first.");

            product.setStatus(ProductStatus.APPROVED);
            return toAdminProductDtoFromMarket(productRepository.save(product));
        }

        // LISTING: existing bridge logic
        Product listing = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        // listing.getSellerId() stores the USER's id, not the seller's primary key
        Seller seller = sellerRepository.findByUserId(listing.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found for product id: " + id));

        if (!"APPROVED".equals(seller.getVerificationStatus()) || !seller.isActive())
            throw new RuntimeException("Cannot approve product: seller account is not yet approved. Approve the seller first.");

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

        // Find the existing PENDING marketplace mirror created during publishListing()
        // If found, just approve it. If not, create it fresh.
        final Category finalCategory = category;
        com.zuply.modules.product.model.Product market = productRepository
                .findBySellerId(seller.getId()).stream()
                .filter(p -> listing.getImageUrl() != null
                        && listing.getImageUrl().equals(p.getImageUrl()))
                .findFirst()
                .orElseGet(() -> {
                    com.zuply.modules.product.model.Product m = new com.zuply.modules.product.model.Product();
                    m.setName(listing.getTitle());
                    m.setDescription(listing.getDescription());
                    m.setSeller(seller);
                    m.setCategory(finalCategory);
                    m.setPrice(listing.getPrice());
                    m.setStock(listing.getStock() != null ? listing.getStock() : 0);
                    m.setImageUrl(listing.getImageUrl());
                    return m;
                });

        // Always sync latest fields and mark APPROVED
        market.setName(listing.getTitle());
        market.setDescription(listing.getDescription());
        market.setSeller(seller);
        market.setCategory(finalCategory);
        market.setPrice(listing.getPrice());
        market.setStock(listing.getStock() != null ? listing.getStock() : 0);
        market.setStatus(ProductStatus.APPROVED);
        productRepository.save(market);

        listing.setStatus("APPROVED");
        return toAdminProductDtoFromListing(listingProductRepository.save(listing));
    }

    /**
     * Reject a product.
     * source = "LISTING" → set status in listing_products table.
     * source = "MANUAL"  → set status in products table.
     */
    @Transactional
    public AdminProductDto rejectProduct(Long id, String source) {
        if ("MANUAL".equals(source)) {
            com.zuply.modules.product.model.Product product = productRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
            product.setStatus(ProductStatus.REJECTED);
            return toAdminProductDtoFromMarket(productRepository.save(product));
        }

        // LISTING
        Product listing = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        listing.setStatus("REJECTED");
        return toAdminProductDtoFromListing(listingProductRepository.save(listing));
    }

    @Transactional
    public AdminProductDto updateProduct(Long id, AdminProductUpdateRequest request) {
        Product product = listingProductRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (request.getTitle()       != null) product.setTitle(request.getTitle());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCategory()    != null) product.setCategory(request.getCategory());
        if (request.getPrice()       != null) product.setPrice(request.getPrice());
        if (request.getStatus()      != null) product.setStatus(request.getStatus());
        return toAdminProductDtoFromListing(listingProductRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        // Try listing table first, then marketplace table
        if (listingProductRepository.existsById(id)) {
            listingProductRepository.deleteById(id);
        } else if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
    }

    // ─── Orders (admin view) ─────────────────────────────────────────────────

    public List<AdminOrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(order -> {
                    String customerName  = order.getCustomerName() != null
                            ? order.getCustomerName()
                            : (order.getCustomer() != null ? order.getCustomer().getName() : "Unknown");
                    String customerEmail = order.getCustomer() != null
                            ? order.getCustomer().getEmail() : "";
                    int itemCount = order.getItems() != null ? order.getItems().size() : 0;
                    String createdAt = order.getCreatedAt() != null
                            ? order.getCreatedAt().toString() : "";
                    return AdminOrderDto.builder()
                            .orderId(order.getId())
                            .customerName(customerName)
                            .customerEmail(customerEmail)
                            .totalAmount(order.getTotalAmount())
                            .status(order.getStatus() != null ? order.getStatus().name() : "PLACED")
                            .paymentMethod(order.getPaymentMethod())
                            .createdAt(createdAt)
                            .itemCount(itemCount)
                            .deliveryAddress(order.getDeliveryAddress())
                            .city(order.getCity())
                            .pincode(order.getPincode())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ─── Reports ─────────────────────────────────────────────────────────────

    public AdminReportDto getReports() {
        BigDecimal totalRevenue = orderRepository.sumAllOrderAmounts();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long totalSellers   = sellerRepository.count();
        long totalCustomers = userRepository.countByRole(Role.CUSTOMER);
        long totalOrders    = orderRepository.count();

        // Merge product counts from both tables:
        // 1. AI/upload listing products (listing_products table)
        Map<String, Long> countByCategory = new java.util.HashMap<>(
                listingProductRepository.findAll().stream()
                        .filter(p -> p.getCategory() != null && !p.getCategory().isBlank())
                        .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()))
        );

        // 2. Approved marketplace products (products table) — merge into the same map
        productRepository.findByStatus(ProductStatus.APPROVED).stream()
                .filter(p -> p.getCategory() != null && p.getCategory().getName() != null
                        && !p.getCategory().getName().isBlank())
                .forEach(p -> countByCategory.merge(p.getCategory().getName(), 1L, Long::sum));

        long total = countByCategory.values().stream().mapToLong(Long::longValue).sum();
        List<AdminReportDto.CategoryStat> categoryBreakdown = countByCategory.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()) // most products first
                .map(e -> new AdminReportDto.CategoryStat(
                        e.getKey(),
                        e.getValue(),
                        total > 0 ? Math.round((e.getValue() * 100.0 / total) * 10.0) / 10.0 : 0.0))
                .collect(Collectors.toList());

        return new AdminReportDto(totalRevenue, totalSellers, totalCustomers, totalOrders, categoryBreakdown);
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
