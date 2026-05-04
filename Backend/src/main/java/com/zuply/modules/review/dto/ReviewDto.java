package com.zuply.modules.review.dto;

public class ReviewDto {
    private Long    id;
    private String  customerName;
    private Integer rating;
    private String  comment;
    private String  createdAt;

    public Long    getId()                   { return id; }
    public void    setId(Long id)            { this.id = id; }

    public String  getCustomerName()         { return customerName; }
    public void    setCustomerName(String n) { this.customerName = n; }

    public Integer getRating()               { return rating; }
    public void    setRating(Integer rating) { this.rating = rating; }

    public String  getComment()              { return comment; }
    public void    setComment(String c)      { this.comment = c; }

    public String  getCreatedAt()            { return createdAt; }
    public void    setCreatedAt(String t)    { this.createdAt = t; }
}
