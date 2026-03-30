package com.zuply.modules.cart.service;

import com.zuply.modules.cart.model.Cart;
import com.zuply.modules.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Optional<Cart> findByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }

    public Cart save(Cart cart) {
        return cartRepository.save(cart);
    }

    public void deleteById(Long id) {
        cartRepository.deleteById(id);
    }
}