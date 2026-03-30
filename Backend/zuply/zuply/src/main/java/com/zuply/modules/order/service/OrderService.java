package com.zuply.modules.order.service;

import com.zuply.common.enums.OrderStatus;
import com.zuply.modules.cart.model.Cart;
import com.zuply.modules.cart.model.CartItem;
import com.zuply.modules.cart.service.CartService;
import com.zuply.modules.order.dto.CheckoutRequest;
import com.zuply.modules.order.dto.OrderDto;
import com.zuply.modules.order.model.Order;
import com.zuply.modules.order.model.OrderItem;
import com.zuply.modules.order.repository.OrderRepository;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private static final List<String> VALID_PAYMENT_METHODS = Arrays.asList("UPI", "CARD", "COD");

    // POST /api/orders - place order from cart
    @Transactional
    public OrderDto placeOrder(CheckoutRequest request) {

        // Validate payment method
        if (!VALID_PAYMENT_METHODS.contains(request.getPaymentMethod().toUpperCase())) {
            throw new RuntimeException("Invalid payment method. Must be UPI, CARD, or COD");
        }

        // Validate delivery address
        DeliveryAddressValidator.validate(request.getDeliveryAddress());

        // Fetch customer
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Build order items — from cart if items not provided, else from request
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        if (request.getItems() != null && !request.getItems().isEmpty()) {
            // Items explicitly provided in request
            for (CheckoutRequest.CheckoutItemDto itemDto : request.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException("Product not found: " + itemDto.getProductId()));

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(product);
                orderItem.setSeller(product.getSeller());
                orderItem.setQuantity(itemDto.getQuantity());
                double price = itemDto.getPrice() != null ? itemDto.getPrice() : product.getPrice();
                orderItem.setPrice(price);
                orderItems.add(orderItem);
                totalAmount += price * itemDto.getQuantity();
            }
        } else {
            // Pull from cart
            Cart cart = cartService.findByCustomerId(request.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Cart is empty. Add items before checkout"));

            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                throw new RuntimeException("Cart must have at least one item before checkout");
            }

            for (CartItem cartItem : cart.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setSeller(cartItem.getProduct().getSeller());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPrice(cartItem.getPrice());
                orderItems.add(orderItem);
                totalAmount += cartItem.getPrice() * cartItem.getQuantity();
            }
        }

        // Validate total amount
        if (totalAmount <= 0) {
            throw new RuntimeException("Total amount must be greater than 0");
        }

        // Build and save order
        Order order = new Order();
        order.setCustomer(customer);
        order.setDeliveryAddress(request.getDeliveryAddress().getAddress());
        order.setCity(request.getDeliveryAddress().getCity());
        order.setPincode(request.getDeliveryAddress().getPincode());
        order.setPaymentMethod(request.getPaymentMethod().toUpperCase());
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PLACED);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Link order items to saved order and save
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        savedOrder.setItems(orderItems);
        orderRepository.save(savedOrder);

        // Clear cart after successful order
        cartService.clearCart(request.getCustomerId());

        return toDto(savedOrder);
    }

    // GET /api/orders?customerId={id} - order history
    public List<OrderDto> getOrdersByCustomer(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return orders.stream().map(this::toDto).collect(Collectors.toList());
    }

    // GET /api/orders/{id} - single order detail
    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toDto(order);
    }

    // Map Order entity to OrderDto
    private OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setOrderId(order.getId());
        dto.setOrderDate(order.getCreatedAt());
        dto.setOrderStatus(order.getStatus());
        dto.setOrderAmount(order.getTotalAmount());
        dto.setPaymentMethod(order.getPaymentMethod());

        if (order.getItems() != null) {
            List<OrderDto.OrderItemDto> itemDtos = order.getItems().stream()
                    .map(item -> new OrderDto.OrderItemDto(
                            item.getId(),
                            item.getProduct().getId(),
                            item.getProduct().getName(),
                            item.getQuantity(),
                            item.getPrice()
                    ))
                    .collect(Collectors.toList());
            dto.setItems(itemDtos);
        }

        return dto;
    }

    // Inner helper for address validation
    private static class DeliveryAddressValidator {
        static void validate(com.zuply.modules.order.dto.DeliveryAddressDto address) {
            if (address == null) throw new RuntimeException("Delivery address is required");
            if (isBlank(address.getCustomerName())) throw new RuntimeException("Customer name must not be empty");
            if (isBlank(address.getPhone())) throw new RuntimeException("Phone must not be empty");
            if (isBlank(address.getAddress())) throw new RuntimeException("Address must not be empty");
            if (isBlank(address.getCity())) throw new RuntimeException("City must not be empty");
            if (isBlank(address.getPincode())) throw new RuntimeException("Pincode must not be empty");
        }

        private static boolean isBlank(String val) {
            return val == null || val.trim().isEmpty();
        }
    }
}