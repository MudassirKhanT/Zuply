package com.zuply.modules.review.model;

import com.zuply.modules.product.model.Product;
import com.zuply.modules.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews",
       uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "user_id"}))
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int rating;           // 1–5

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }

    public Product getProduct()                { return product; }
    public void setProduct(Product product)    { this.product = product; }

    public User getUser()                      { return user; }
    public void setUser(User user)             { this.user = user; }

    public int getRating()                     { return rating; }
    public void setRating(int rating)          { this.rating = rating; }

    public String getComment()                 { return comment; }
    public void setComment(String comment)     { this.comment = comment; }

    public LocalDateTime getCreatedAt()                    { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)      { this.createdAt = createdAt; }
}
