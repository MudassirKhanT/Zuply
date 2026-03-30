package com.zuply.modules.cart.service;

import com.zuply.modules.cart.dto.CartDto;
import com.zuply.modules.cart.dto.CartItemDto;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // GET /api/cart - return full cart with items and grand total
    public CartDto getCartByCustomerId(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Cart not found for customer"));

        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());

        return new CartDto(cart.getId(), customerId, itemDtos);
    }

    // POST /api/cart - add item to cart (creates cart if not exists)
    public CartDto addItemToCart(Long customerId, CartItemRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Get or create cart
        Cart cart = cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setCustomer(customer);
            newCart.setItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        // Check if product already in cart — if so, increase quantity
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(request.getQuantity());
            newItem.setPrice(request.getPrice() != null ? request.getPrice() : product.getPrice());
            cartItemRepository.save(newItem);
        }

        return getCartByCustomerId(customerId);
    }

    // PUT /api/cart/{itemId} - update quantity; if 0 or less, remove item
    public CartDto updateCartItemQuantity(Long customerId, Long itemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.deleteById(itemId);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return getCartByCustomerId(customerId);
    }

    // DELETE /api/cart/{itemId} - remove single item
    public CartDto removeItemFromCart(Long customerId, Long itemId) {
        cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.deleteById(itemId);
        return getCartByCustomerId(customerId);
    }

    // Used by OrderService after checkout
    public void clearCart(Long customerId) {
        cartRepository.findByCustomerId(customerId).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepository.save(cart);
        });
    }

    public Optional<Cart> findByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    // Map CartItem entity to DTO
    private CartItemDto toItemDto(CartItem item) {
        return new CartItemDto(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}