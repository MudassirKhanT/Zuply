package com.zuply.modules.wishlist.service;

import com.zuply.modules.product.model.Product;
import com.zuply.modules.product.repository.ProductRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import com.zuply.modules.wishlist.dto.WishlistItemDto;
import com.zuply.modules.wishlist.model.Wishlist;
import com.zuply.modules.wishlist.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // GET - return wishlist as DTOs with product details
    public List<WishlistItemDto> getWishlistByCustomerId(Long customerId) {
        List<Wishlist> items = wishlistRepository.findByCustomerId(customerId);
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    // POST - add product to wishlist, prevent duplicates
    public WishlistItemDto addToWishlist(Long customerId, Long productId) {
        // Duplicate check
        if (wishlistRepository.findByCustomerIdAndProductId(customerId, productId).isPresent()) {
            throw new RuntimeException("Product already in wishlist");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Wishlist wishlist = new Wishlist();
        wishlist.setCustomer(customer);
        wishlist.setProduct(product);

        Wishlist saved = wishlistRepository.save(wishlist);
        return toDto(saved);
    }

    // DELETE - remove product from wishlist
    @Transactional
    public void removeFromWishlist(Long customerId, Long productId) {
        wishlistRepository.findByCustomerIdAndProductId(customerId, productId)
                .orElseThrow(() -> new RuntimeException("Item not found in wishlist"));
        wishlistRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }

    public boolean existsByCustomerIdAndProductId(Long customerId, Long productId) {
        return wishlistRepository.findByCustomerIdAndProductId(customerId, productId).isPresent();
    }

    // Map Wishlist entity to DTO
    private WishlistItemDto toDto(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        String sellerName = (product.getSeller() != null && product.getSeller().getStoreName() != null)
                ? product.getSeller().getStoreName()
                : "Unknown Seller";

        return new WishlistItemDto(
                wishlist.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                sellerName,
                product.getImageUrl()
        );
    }
}