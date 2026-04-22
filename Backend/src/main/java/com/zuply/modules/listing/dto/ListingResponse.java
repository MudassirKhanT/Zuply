package com.zuply.modules.listing.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ListingResponse {
    private Long productId;
    private Long imageId;
    private String originalImageUrl;
    private String processedImageUrl;
    private String title;
    private String description;
    private String category;
    private boolean aiSuggestedCategory;
    private String color;
    private String material;
    private String productType;
    private Double price;
    private String suggestedPriceMin;
    private String suggestedPriceMax;
    private List<String> tags;
    private List<String> highlights;
    private String status;
}
