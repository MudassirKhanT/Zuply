package com.zuply.modules.listing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublishResponse {
    private Long productId;
    private String status;
    private String title;
    private String message;
}
