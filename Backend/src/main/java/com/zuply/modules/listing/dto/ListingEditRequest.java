package com.zuply.modules.listing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class ListingEditRequest {

    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "10000000.00", message = "Price must not exceed ₹1,00,00,000")
    private Double price;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Size(max = 100, message = "Material must not exceed 100 characters")
    private String material;

    @Size(max = 100, message = "Product type must not exceed 100 characters")
    private String productType;

    @Size(max = 10, message = "Highlights must not exceed 10 items")
    private List<String> highlights;

    @Size(max = 15, message = "Tags must not exceed 15 items")
    private List<String> tags;
}
