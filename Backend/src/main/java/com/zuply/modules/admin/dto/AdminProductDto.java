package com.zuply.modules.admin.dto;

import lombok.*;

/**
 * Unified product DTO returned by the admin products endpoint.
 * Normalises fields from both the listing_products table (AI/seller upload flow)
 * and the products table (manual seller creation flow).
 *
 * source = "LISTING" → originated in listing_products
 * source = "MANUAL"  → originated in products (manually created)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductDto {

    private Long    id;
    private String  name;
    private String  description;
    private String  category;
    private Double  price;
    private Integer stock;
    private String  status;       // PENDING | APPROVED | REJECTED
    private String  sellerName;
    private Long    sellerUserId; // user-id of the seller (for reference)
    private String  imageUrl;
    private String  source;       // LISTING | MANUAL
    private boolean aiGenerated;
}
