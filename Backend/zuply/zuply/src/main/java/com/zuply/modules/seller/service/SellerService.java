package com.zuply.modules.seller.service;

import com.zuply.common.enums.OrderStatus;
import com.zuply.modules.order.model.Order;
import com.zuply.modules.order.model.OrderItem;
import com.zuply.modules.order.repository.OrderItemRepository;
import com.zuply.modules.order.repository.OrderRepository;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.seller.dto.SellerDashboardDto;
import com.zuply.modules.seller.dto.SellerOrderDto;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    public List<Seller> findAll() {
        return sellerRepository.findAll();
    }

    public Optional<Seller> findById(Long id) {
        return sellerRepository.findById(id);
    }

    public Optional<Seller> findByUserId(Long userId) {
        return sellerRepository.findByUserId(userId);
    }

    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    // --- Seller Registration ---
    public Seller registerSeller(User user, String storeName, String location, String pincode) {
        Seller seller = new Seller();
        seller.setUser(user);
        seller.setStoreName(storeName);
        seller.setLocation(location);
        seller.setPincode(pincode);
        seller.setVerificationStatus("PENDING");
        seller.setActive(false);
        return sellerRepository.save(seller);
    }

    // --- Seller Dashboard ---
    public SellerDashboardDto getDashboard(Long sellerId) {
        long totalProducts = productRepository.findBySellerId(sellerId).size();

        List<OrderItem> sellerOrderItems = orderItemRepository.findBySellerId(sellerId);
        long totalOrders = sellerOrderItems.stream()
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();

        long pendingOrders = sellerOrderItems.stream()
                .filter(item -> item.getOrder().getStatus() == OrderStatus.PLACED)
                .map(item -> item.getOrder().getId())
                .distinct()
                .count();

        return new SellerDashboardDto(totalProducts, totalOrders, pendingOrders);
    }

    // --- Seller Products (own products only) ---
    public List<Product> getSellerProducts(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    // --- Seller Orders (orders containing seller's products) ---
    public List<SellerOrderDto> getSellerOrders(Long sellerId) {
        List<OrderItem> sellerOrderItems = orderItemRepository.findBySellerId(sellerId);
        List<SellerOrderDto> orderDtos = new ArrayList<>();

        for (OrderItem item : sellerOrderItems) {
            Order order = item.getOrder();
            String customerName = order.getCustomerName() != null
                    ? order.getCustomerName()
                    : (order.getCustomer() != null ? order.getCustomer().getName() : "Unknown");
            SellerOrderDto dto = new SellerOrderDto(
                    order.getId(),
                    customerName,
                    item.getProduct().getName(),
                    item.getQuantity(),
                    order.getStatus().name()
            );
            orderDtos.add(dto);
        }

        return orderDtos;
    }

    // --- Update Order Status (forward only) ---
    public Order updateOrderStatus(Long sellerId, Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Validate seller owns this order (has items in it)
        List<OrderItem> sellerItems = orderItemRepository.findBySellerId(sellerId);
        boolean ownsOrder = sellerItems.stream()
                .anyMatch(item -> item.getOrder().getId().equals(orderId));
        if (!ownsOrder) {
            throw new RuntimeException("You do not have access to this order");
        }

        OrderStatus current = order.getStatus();
        OrderStatus target;
        try {
            target = OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid order status: " + newStatus);
        }

        // Enforce forward-only transitions
        if (target.ordinal() <= current.ordinal()) {
            throw new RuntimeException("Cannot change status backward");
        }

        // Enforce single-step transitions: PLACED -> PROCESSING -> DELIVERED
        if (target.ordinal() - current.ordinal() != 1) {
            throw new RuntimeException("Invalid status transition. Must follow PLACED → PROCESSING → DELIVERED");
        }

        order.setStatus(target);
        return orderRepository.save(order);
    }
}
