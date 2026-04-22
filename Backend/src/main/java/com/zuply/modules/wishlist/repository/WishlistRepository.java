package com.zuply.modules.wishlist.repository;

import com.zuply.modules.wishlist.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    List<Wishlist> findByCustomerId(Long customerId);

    Optional<Wishlist> findByCustomerIdAndProductId(Long customerId, Long productId);

    @Transactional   // ADDED — required for derived delete queries
    void deleteByCustomerIdAndProductId(Long customerId, Long productId);
}
