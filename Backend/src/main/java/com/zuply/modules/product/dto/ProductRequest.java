package com.zuply.modules.product.dto;

import java.util.List;

public class ProductRequest {

    private String name;
    private String description;
    private Long categoryId;
    private Double price;
    private Integer stock;
    private String variations;
    private String deliveryMethod;
    private String returnPolicy;
    private String imageUrl;
    private List<String> extraImages;
    private Long sellerId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getVariations() { return variations; }
    public void setVariations(String variations) { this.variations = variations; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod; }

    public String getReturnPolicy() { return returnPolicy; }
    public void setReturnPolicy(String returnPolicy) {
        this.returnPolicy = returnPolicy; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getExtraImages() { return extraImages; }
    public void setExtraImages(List<String> extraImages) { this.extraImages = extraImages; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
}