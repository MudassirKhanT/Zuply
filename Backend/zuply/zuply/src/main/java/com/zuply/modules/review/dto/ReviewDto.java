package com.zuply.modules.review.dto;

import com.zuply.modules.review.model.Review;
import java.time.format.DateTimeFormatter;

public class ReviewDto {

    private Long   id;
    private String reviewerName;
    private int    rating;
    private String comment;
    private String createdAt;

    public ReviewDto() {}

    public ReviewDto(Review r) {
        this.id           = r.getId();
        this.reviewerName = r.getUser() != null ? r.getUser().getName() : "Customer";
        this.rating       = r.getRating();
        this.comment      = r.getComment();
        this.createdAt    = r.getCreatedAt() != null
                ? r.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "";
    }

    public Long   getId()                      { return id; }
    public void   setId(Long id)               { this.id = id; }

    public String getReviewerName()            { return reviewerName; }
    public void   setReviewerName(String n)    { this.reviewerName = n; }

    public int  getRating()                    { return rating; }
    public void setRating(int r)               { this.rating = r; }

    public String getComment()                 { return comment; }
    public void   setComment(String c)         { this.comment = c; }

    public String getCreatedAt()               { return createdAt; }
    public void   setCreatedAt(String d)       { this.createdAt = d; }
}
