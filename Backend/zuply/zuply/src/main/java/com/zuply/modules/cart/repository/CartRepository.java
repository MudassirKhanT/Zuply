package com.zuply.modules.cart.repository;

import com.zuply.modules.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer_Id(Long customerId);  // FIXED: was findByCustomerId
}
