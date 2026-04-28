package com.zuply.modules.cart.service;

import com.zuply.modules.cart.dto.CartDto;
import com.zuply.modules.cart.dto.CartItemRequest;
import com.zuply.modules.cart.model.Cart;
import com.zuply.modules.cart.model.CartItem;
import com.zuply.modules.cart.repository.CartItemRepository;
import com.zuply.modules.cart.repository.CartRepository;
import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // ── Get or create cart ────────────────────────────────────────────────────

    private Cart getOrCreateCart(Long userId) {
        // Uses findByCustomer_Id (underscore) — required because Cart.customer
        // is a User entity, not a Long. See CartRepository fix (Bug 4).
        return cartRepository.findByCustomer_Id(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Cart newCart = new Cart();
            newCart.setCustomer(user);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });
    }

    public Optional<Cart> findByCustomerId(Long customerId) {
        return cartRepository.findByCustomer_Id(customerId);
    }

    // ── DTO mapper ────────────────────────────────────────────────────────────

    private CartDto toDto(Cart cart) {
        List<CartDto.CartItemDto> itemDtos = cart.getItems() == null
                ? new ArrayList<>()
                : cart.getItems().stream().map(item -> {
            CartDto.CartItemDto dto = new CartDto.CartItemDto();
            dto.setItemId(item.getId());
            dto.setProductId(item.getProduct().getId());
            dto.setProductName(item.getProduct().getName());
            dto.setQuantity(item.getQuantity());
            dto.setPricePerUnit(item.getPrice());
            dto.setTotalPrice(item.getPrice() * item.getQuantity());
            return dto;
        }).collect(Collectors.toList());

        double grandTotal = itemDtos.stream()
                .mapToDouble(CartDto.CartItemDto::getTotalPrice)
                .sum();

        CartDto dto = new CartDto();
        dto.setCartId(cart.getId());
        dto.setItems(itemDtos);
        dto.setGrandTotal(grandTotal);
        return dto;
    }

    // ── Public API ────────────────────────────────────────────────────────────


    public CartDto getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return toDto(cart);
    }

    @Transactional
    public CartDto addItem(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // If product already in cart, increase quantity
        Optional<CartItem> existing = cart.getItems() == null
                ? Optional.empty()
                : cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            item.setPrice(product.getPrice());
            cartItemRepository.save(item);
        }

        Cart updated = cartRepository.findById(cart.getId()).orElse(cart);
        return toDto(updated);
    }


    @Transactional
    public CartDto updateItemQuantity(Long userId, Long itemId, int quantity) {
        Cart cart = cartRepository.findByCustomer_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.deleteByItemId(itemId);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        Cart updated = cartRepository.findByCustomer_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return toDto(updated);
    }

    @Transactional
    public CartDto removeItem(Long userId, Long itemId) {
        Cart cart = cartRepository.findByCustomer_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        boolean found = cart.getItems().stream().anyMatch(i -> i.getId().equals(itemId));
        if (!found) throw new RuntimeException("Cart item not found");

        cartItemRepository.deleteByItemId(itemId);

        Cart updated = cartRepository.findByCustomer_Id(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        return toDto(updated);
    }

    @Transactional
    public void clearCart(Long userId) {
        cartRepository.findByCustomer_Id(userId).ifPresent(cart ->
            cartItemRepository.deleteAllByCartId(cart.getId())
        );
    }
}