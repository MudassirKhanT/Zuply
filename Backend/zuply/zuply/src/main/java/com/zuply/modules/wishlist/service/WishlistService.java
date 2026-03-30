package com.zuply.modules.wishlist.service;

import com.zuply.modules.wishlist.model.Wishlist;
import com.zuply.modules.wishlist.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    public List<Wishlist> findByCustomerId(Long customerId) {
        return wishlistRepository.findByCustomerId(customerId);
    }

    public Wishlist save(Wishlist wishlist) {
        return wishlistRepository.save(wishlist);
    }

    public void removeByCustomerIdAndProductId(Long customerId, Long productId) {
        wishlistRepository.deleteByCustomerIdAndProductId(customerId, productId);
    }

    public boolean existsByCustomerIdAndProductId(Long customerId, Long productId) {
        return wishlistRepository.findByCustomerIdAndProductId(
                customerId, productId).isPresent();
    }
}