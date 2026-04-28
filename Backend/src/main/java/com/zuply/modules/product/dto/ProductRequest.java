package com.zuply.modules.product.dto;

import jakarta.validation.constraints.*;

public class ProductRequest {

    @NotBlank(message = "Product name must not be blank")
    @Size(min = 3, max = 150, message = "Product name must be between 3 and 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Category must not be null")
    private Long categoryId;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "10000000.00", message = "Price must not exceed ₹1,00,00,000")
    private Double price;

    @NotNull(message = "Stock must not be null")
    @Min(value = 0, message = "Stock cannot be negative")
    @Max(value = 100000, message = "Stock must not exceed 1,00,000 units")
    private Integer stock;

    @Size(max = 200, message = "Variations must not exceed 200 characters")
    private String variations;

    @Size(max = 100, message = "Delivery method must not exceed 100 characters")
    private String deliveryMethod;

    @Size(max = 500, message = "Return policy must not exceed 500 characters")
    private String returnPolicy;

    private String imageUrl;

    private Long sellerId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getVariations() { return variations; }
    public void setVariations(String variations) { this.variations = variations; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getReturnPolicy() { return returnPolicy; }
    public void setReturnPolicy(String returnPolicy) { this.returnPolicy = returnPolicy; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
}
