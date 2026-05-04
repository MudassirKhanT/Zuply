package com.zuply.modules.listing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity(name = "ListingProduct")
@Table(name = "listing_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_id")
    private Long imageId;

    @Column(name = "seller_id")
    private Long sellerId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String category;
    private String color;
    private String material;
    private String productType;
    private String suggestedPriceMin;
    private String suggestedPriceMax;

    @Column(columnDefinition = "TEXT")
    private String highlights;

    private String imageUrl;

    @Column(name = "extra_images", columnDefinition = "TEXT")
    private String extraImages; // comma-separated additional image URLs in display order

    private Double price;
    private boolean aiSuggestedCategory;
    private String status;
}
