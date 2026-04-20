package com.zuply.modules.ai.dto;

import lombok.*;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIGeneratedContent {

    // Generated product title
    private String title;

    // Generated product description
    private String description;

    // Extracted attributes
    private String color;
    private String material;
    private String productType;

    // Suggested price range (values come as strings, e.g. "499" or "499.00")
    private String suggestedPriceMin;
    private String suggestedPriceMax;

    // Key selling highlights (3-5 bullet points)
    private List<String> highlights;

    // Search optimization tags (5-10 keywords)
    private List<String> tags;

    // One of the predefined Zuply categories
    private String suggestedCategory;
}