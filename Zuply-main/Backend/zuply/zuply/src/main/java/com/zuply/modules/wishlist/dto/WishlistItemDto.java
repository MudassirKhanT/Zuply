package com.zuply.modules.wishlist.dto;

public class WishlistItemDto {

    private Long wishlistId;
    private Long productId;
    private String productName;
    private Double price;
    private String sellerName;
    private String imageUrl;

    public WishlistItemDto() {}

    public WishlistItemDto(Long wishlistId, Long productId, String productName,
                           Double price, String sellerName, String imageUrl) {
        this.wishlistId = wishlistId;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.sellerName = sellerName;
        this.imageUrl = imageUrl;
    }

    public Long getWishlistId() { return wishlistId; }
    public void setWishlistId(Long wishlistId) { this.wishlistId = wishlistId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}