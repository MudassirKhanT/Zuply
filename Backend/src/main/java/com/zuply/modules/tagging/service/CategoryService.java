package com.zuply.modules.tagging.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CategoryService
 * ---------------
 * Maps the AI-suggested category string to one of Zuply's
 * predefined categories. Allows seller manual override.
 *
 * ASSIGNED TO: Manjunath
 *
 * TODO:
 *  1. Define the predefined Zuply category list (same as Sprint 1)
 *  2. Accept AI-suggested category string
 *  3. Match it to the closest predefined category (case-insensitive)
 *  4. If no match, default to "Other"
 *  5. Return the matched category string
 */
@Service("taggingCategoryService")
public class CategoryService {

    // Predefined Zuply categories (must match Sprint 1 category list exactly)
    private static final List<String> PREDEFINED_CATEGORIES = List.of(
            "Electronics",
            "Clothing",
            "Grocery",
            "Food & Beverage",
            "Home & Kitchen",
            "Beauty & Personal Care",
            "Health & Wellness",
            "Agriculture",
            "Fashion & Footwear",
            "Other"
    );

    public String mapCategory(String aiSuggestedCategory) {

        // Step 1 — Null/empty check
        if (aiSuggestedCategory == null || aiSuggestedCategory.isBlank()) return "Other";

        // Step 2 — Case-insensitive match against predefined list
        return PREDEFINED_CATEGORIES.stream()
            .filter(cat -> cat.equalsIgnoreCase(aiSuggestedCategory.trim()))
            .findFirst()
            .orElse("Other");
    }

    public List<String> getAllCategories() {
        return PREDEFINED_CATEGORIES;
    }
}
