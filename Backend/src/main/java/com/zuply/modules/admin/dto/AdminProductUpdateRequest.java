package com.zuply.modules.admin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AdminProductUpdateRequest {

    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @Pattern(regexp = "^(DRAFT|PUBLISHED)$", message = "Status must be DRAFT or PUBLISHED")
    private String status;
}
