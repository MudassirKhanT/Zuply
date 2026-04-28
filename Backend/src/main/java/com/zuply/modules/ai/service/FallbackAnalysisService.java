package com.zuply.modules.ai.service;

import com.zuply.modules.ai.dto.AIGeneratedContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

/**
 * Zero-API fallback that derives what it can from the image itself,
 * then returns a pre-filled draft so the seller's listing flow never crashes.
 *
 * What it actually does:
 *  - Samples the image pixels to find the dominant color name
 *  - Scans the filename for category keywords (e.g. "shirt", "phone", "rice")
 *  - Returns a completely filled AIGeneratedContent so the pipeline succeeds,
 *    with the title clearly marked "[Edit Required]" so sellers know to review it
 */
@Slf4j
@Service
public class FallbackAnalysisService {

    public AIGeneratedContent analyze(String imagePath) {
        log.info("Running local fallback analysis on: {}", imagePath);

        String dominantColor = extractDominantColor(imagePath);
        String filename = Paths.get(imagePath).getFileName().toString().toLowerCase();
        CategoryHint hint = detectCategoryFromFilename(filename);

        return AIGeneratedContent.builder()
                .title("[Edit Required] " + hint.productType)
                .description("Please update this description with accurate product details. " +
                        "This listing was created without AI assistance because the AI service " +
                        "is temporarily unavailable.")
                .color(dominantColor)
                .material("Please specify")
                .productType(hint.productType)
                .suggestedPriceMin(hint.priceMin)
                .suggestedPriceMax(hint.priceMax)
                .highlights(List.of(
                        "Update this highlight with a key feature",
                        "Add another selling point here",
                        "Describe what makes this product unique"
                ))
                .tags(hint.tags)
                .suggestedCategory(hint.category)
                .build();
    }

    // ── Color Extraction ──────────────────────────────────────────────────────

    private String extractDominantColor(String imagePath) {
        try {
            BufferedImage img = ImageIO.read(new File(imagePath));
            if (img == null) return "Unknown";

            // Sample a grid of pixels (max 20x20 = 400 samples) for speed
            int sampleStep = Math.max(1, Math.min(img.getWidth(), img.getHeight()) / 20);
            Map<String, Integer> colorCounts = new HashMap<>();

            for (int y = 0; y < img.getHeight(); y += sampleStep) {
                for (int x = 0; x < img.getWidth(); x += sampleStep) {
                    int argb = img.getRGB(x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    if (alpha < 128) continue; // skip transparent pixels (background-removed images)

                    String name = toColorName(
                            (argb >> 16) & 0xFF,
                            (argb >> 8) & 0xFF,
                            argb & 0xFF
                    );
                    colorCounts.merge(name, 1, Integer::sum);
                }
            }

            return colorCounts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");

        } catch (IOException e) {
            log.warn("Could not read image for color extraction: {}", e.getMessage());
            return "Unknown";
        }
    }

    private String toColorName(int r, int g, int b) {
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float hue = hsb[0] * 360;
        float sat = hsb[1];
        float bri = hsb[2];

        if (bri < 0.15f) return "Black";
        if (bri > 0.85f && sat < 0.15f) return "White";
        if (sat < 0.15f) return bri > 0.5f ? "Light Grey" : "Dark Grey";

        if (hue < 15 || hue >= 345) return "Red";
        if (hue < 45)  return "Orange";
        if (hue < 70)  return "Yellow";
        if (hue < 150) return "Green";
        if (hue < 195) return "Cyan";
        if (hue < 260) return "Blue";
        if (hue < 290) return "Purple";
        if (hue < 345) return "Pink";
        return "Unknown";
    }

    // ── Category Detection from Filename ─────────────────────────────────────

    private CategoryHint detectCategoryFromFilename(String filename) {

        // Electronics
        if (anyMatch(filename, "phone", "mobile", "laptop", "computer", "tablet",
                "headphone", "earphone", "speaker", "camera", "tv", "monitor",
                "keyboard", "mouse", "charger", "cable", "battery", "watch", "smartwatch")) {
            return new CategoryHint("Electronics", "Electronic Device",
                    "999", "4999", List.of("electronics", "gadget", "tech", "device", "digital"));
        }

        // Clothing
        if (anyMatch(filename, "shirt", "tshirt", "t-shirt", "jeans", "pants", "trouser",
                "dress", "skirt", "jacket", "coat", "hoodie", "sweater", "kurta", "saree")) {
            return new CategoryHint("Clothing", "Clothing Item",
                    "299", "1499", List.of("clothing", "fashion", "apparel", "wear", "outfit"));
        }

        // Fashion & Footwear
        if (anyMatch(filename, "shoe", "sandal", "slipper", "boot", "heel", "sneaker",
                "bag", "purse", "wallet", "belt", "watch", "sunglasses", "accessory")) {
            return new CategoryHint("Fashion & Footwear", "Fashion Accessory",
                    "199", "1999", List.of("fashion", "footwear", "accessory", "style", "trendy"));
        }

        // Home & Kitchen
        if (anyMatch(filename, "cup", "mug", "plate", "bowl", "pot", "pan", "bottle",
                "jar", "box", "container", "basket", "lamp", "pillow", "curtain", "mat",
                "chair", "table", "shelf", "rack", "organizer")) {
            return new CategoryHint("Home & Kitchen", "Home Product",
                    "199", "1499", List.of("home", "kitchen", "household", "decor", "utility"));
        }

        // Beauty & Personal Care
        if (anyMatch(filename, "cream", "lotion", "serum", "shampoo", "soap", "perfume",
                "lipstick", "makeup", "foundation", "moisturizer", "sunscreen", "oil")) {
            return new CategoryHint("Beauty & Personal Care", "Beauty Product",
                    "149", "999", List.of("beauty", "skincare", "personal care", "cosmetics", "wellness"));
        }

        // Grocery / Food
        if (anyMatch(filename, "rice", "wheat", "flour", "sugar", "oil", "spice", "masala",
                "dal", "lentil", "grain", "seed", "snack", "biscuit", "chocolate", "tea",
                "coffee", "juice", "sauce", "pickle")) {
            return new CategoryHint("Grocery", "Food Product",
                    "49", "499", List.of("grocery", "food", "organic", "natural", "fresh"));
        }

        // Agriculture
        if (anyMatch(filename, "fertilizer", "seed", "plant", "crop", "farm", "soil",
                "pesticide", "tool", "harvest", "vegetable", "fruit")) {
            return new CategoryHint("Agriculture", "Agricultural Product",
                    "99", "999", List.of("agriculture", "farming", "organic", "natural", "crop"));
        }

        // Health & Wellness
        if (anyMatch(filename, "supplement", "vitamin", "protein", "medicine", "tablet",
                "capsule", "syrup", "health", "fitness", "yoga", "gym")) {
            return new CategoryHint("Health & Wellness", "Health Product",
                    "199", "1499", List.of("health", "wellness", "fitness", "supplement", "natural"));
        }

        // Default
        return new CategoryHint("Home & Kitchen", "Product",
                "199", "999", List.of("product", "quality", "value", "new", "sale"));
    }

    private boolean anyMatch(String filename, String... keywords) {
        for (String kw : keywords) {
            if (filename.contains(kw)) return true;
        }
        return false;
    }

    private static class CategoryHint {
        final String category;
        final String productType;
        final String priceMin;
        final String priceMax;
        final List<String> tags;

        CategoryHint(String category, String productType,
                     String priceMin, String priceMax, List<String> tags) {
            this.category = category;
            this.productType = productType;
            this.priceMin = priceMin;
            this.priceMax = priceMax;
            this.tags = tags;
        }
    }
}
