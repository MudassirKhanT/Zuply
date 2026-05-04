package com.zuply.config;

import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.category.repository.CategoryRepository;
import com.zuply.modules.listing.model.Product;
import com.zuply.modules.listing.repository.ProductRepository;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Runs once at startup to migrate published AI listing products that were
 * not yet reflected in the main products table (e.g. published before the
 * publishListing mirror fix was deployed).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ListingMigrationRunner implements ApplicationRunner {

    private final ProductRepository listingProductRepository;
    private final com.zuply.modules.product.repository.ProductRepository mainProductRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Product> published = listingProductRepository.findAll().stream()
                .filter(p -> "PUBLISHED".equals(p.getStatus()))
                .toList();

        if (published.isEmpty()) return;

        int migrated = 0;
        for (Product listing : published) {
            try {
                Seller seller = resolveOrCreateSeller(listing.getSellerId());
                if (seller == null) continue;

                boolean alreadyMirrored = mainProductRepository.findBySellerId(seller.getId())
                        .stream()
                        .anyMatch(p -> listing.getImageUrl() != null
                                && listing.getImageUrl().equals(p.getImageUrl()));

                if (!alreadyMirrored) {
                    com.zuply.modules.product.model.Product main = new com.zuply.modules.product.model.Product();
                    main.setName(listing.getTitle());
                    main.setDescription(listing.getDescription());
                    main.setPrice(listing.getPrice());
                    main.setStock(1);
                    main.setImageUrl(listing.getImageUrl());
                    main.setExtraImages(listing.getExtraImages());
                    main.setSeller(seller);
                    main.setStatus(ProductStatus.PENDING);

                    if (listing.getCategory() != null) {
                        categoryRepository.findByNameIgnoreCase(listing.getCategory())
                                .ifPresent(main::setCategory);
                    }

                    mainProductRepository.save(main);
                    migrated++;
                }
            } catch (Exception e) {
                log.warn("Failed to migrate listing product id={}: {}", listing.getId(), e.getMessage());
            }
        }

        if (migrated > 0) {
            log.info("Migrated {} published AI listing product(s) into the main products table.", migrated);
        }
    }

    private Seller resolveOrCreateSeller(Long userId) {
        return sellerRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return null;
            Seller s = new Seller();
            s.setUser(user);
            s.setStoreName(user.getName() + "'s Store");
            s.setVerificationStatus("PENDING");
            s.setActive(false);
            return sellerRepository.save(s);
        });
    }
}
