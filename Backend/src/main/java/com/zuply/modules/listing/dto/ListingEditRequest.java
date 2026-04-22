package com.zuply.modules.listing.dto;

import lombok.Data;
import java.util.List;

@Data
public class ListingEditRequest {
    private String title;
    private String description;
    private String category;
    private Double price;
    private String color;
    private String material;
    private String productType;
    private List<String> highlights;
    private List<String> tags;
}
