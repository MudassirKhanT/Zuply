package com.zuply.modules.seller.service;

import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

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
}