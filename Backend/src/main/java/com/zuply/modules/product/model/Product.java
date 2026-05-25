package com.zuply.modules.product.model;

import com.zuply.common.enums.ProductStatus;
import com.zuply.modules.category.model.Category;
import com.zuply.modules.seller.model.Seller;
import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private Seller seller;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    @Column(columnDefinition = "INT DEFAULT 1")
    private Integer stock = 1;
    private String variations;
    private String deliveryMethod;
    private String returnPolicy;
    private String imageUrl;

    @Column(name = "extra_images", columnDefinition = "TEXT")
    private String extraImages; // comma-separated additional image URLs in display order

    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.PENDING;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Seller getSeller() { return seller; }
    public void setSeller(Seller seller) { this.seller = seller; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

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

    public String getExtraImages() { return extraImages; }
    public void setExtraImages(String extraImages) { this.extraImages = extraImages; }

    public ProductStatus getStatus() { return status; }
    public void setStatus(ProductStatus status) { this.status = status; }
}