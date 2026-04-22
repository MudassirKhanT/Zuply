package com.zuply.modules.tagging.dto;

import lombok.*;
import java.util.List;

/**
 * TagResult
 * ---------
 * Returned after tag generation and category classification.
 *
 * ASSIGNED TO: Manjunath
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResult {

    private Long productId;
    private List<String> tags;
    private String assignedCategory;

    // true = assigned by AI, false = manually overridden by seller
    private boolean aiSuggested;
}
