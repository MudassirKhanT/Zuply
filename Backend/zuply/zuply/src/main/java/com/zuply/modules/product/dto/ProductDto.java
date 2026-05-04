package com.zuply.modules.product.dto;

import java.util.List;

public class ProductDto {

    private Long id;
    private String name;
    private String description;
    private String categoryName;
    private Double price;
    private Integer stock;
    private String variations;
    private String deliveryMethod;
    private String returnPolicy;
    private String imageUrl;
    private List<String> extraImages;
    private String sellerName;
    private String sellerPincode;
    private String status;
    private double averageRating;
    private long   reviewCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName; }

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

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getSellerPincode() { return sellerPincode; }
    public void setSellerPincode(String sellerPincode) {
        this.sellerPincode = sellerPincode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAverageRating()              { return averageRating; }
    public void   setAverageRating(double r)      { this.averageRating = r; }

    public long getReviewCount()                  { return reviewCount; }
    public void setReviewCount(long c)            { this.reviewCount = c; }
}