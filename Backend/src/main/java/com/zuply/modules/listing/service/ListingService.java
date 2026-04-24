package com.zuply.modules.listing.service;

import com.zuply.modules.ai.dto.AIGeneratedContent;
import com.zuply.modules.ai.service.GeminiService;
import com.zuply.modules.listing.dto.ListingEditRequest;
import com.zuply.modules.listing.dto.ListingResponse;
import com.zuply.modules.listing.dto.PublishResponse;
import com.zuply.modules.listing.model.Product;
import com.zuply.modules.listing.repository.ProductRepository;
import com.zuply.modules.processing.service.ProcessingService;
import com.zuply.modules.tagging.repository.TagRepository;
import com.zuply.modules.tagging.service.CategoryService;
import com.zuply.modules.tagging.service.TagService;
import com.zuply.modules.upload.dto.ImageStatus;
import com.zuply.modules.upload.model.Image;
import com.zuply.modules.upload.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class ListingService {

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProcessingService processingService;
    private final GeminiService geminiService;
    private final TagService tagService;
    private final CategoryService categoryService;

    // -------------------------------------------------------
    // Trigger full pipeline for an uploaded image
    // -------------------------------------------------------
    public ListingResponse generateListing(Long imageId, Long sellerId) throws Exception {

        // Step 1 — Fetch Image record by imageId
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        // Step 2 — Run image processing pipeline (background removal + enhancement)
        // This updates the Image record in DB with processedUrl and status = PROCESSED
        processingService.processImage(imageId);

        // Step 3 — Re-fetch Image so we have the updated processedUrl
        Image processed = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found after processing"));

        // Step 4 — Call Gemini Vision API with the processed image path
        AIGeneratedContent ai = geminiService.generateContent(processed.getProcessedUrl());

        // Step 5 — Map the AI-suggested category to a Zuply predefined category
        String category = categoryService.mapCategory(ai.getSuggestedCategory());

        // Step 6 — Build Product entity with all AI-generated fields and save as DRAFT
        // Highlights list is joined into a pipe-separated string for DB storage
        String highlightsString = (ai.getHighlights() != null && !ai.getHighlights().isEmpty())
                ? String.join("|", ai.getHighlights())
                : "";

        Product product = Product.builder()
                .imageId(imageId)
                .sellerId(sellerId)
                .title(ai.getTitle())
                .description(ai.getDescription())
                .category(category)
                .color(ai.getColor())
                .material(ai.getMaterial())
                .productType(ai.getProductType())
                .suggestedPriceMin(ai.getSuggestedPriceMin())
                .suggestedPriceMax(ai.getSuggestedPriceMax())
                .highlights(highlightsString)
                .imageUrl(processed.getProcessedUrl())
                .aiSuggestedCategory(true)
                .status("DRAFT")
                .build();

        Product saved = productRepository.save(product);

        // Step 7 — Save tags to the tags table (TagService handles deduplication)
        tagService.saveTags(saved.getId(), ai.getTags());

        // Step 8 — Update Image status to COMPLETED — pipeline is done
        processed.setStatus(ImageStatus.COMPLETED);
        imageRepository.save(processed);

        // Build and return the full listing response
        return buildListingResponse(saved);
    }


    // Fetch listing preview for seller review
    public ListingResponse getListing(Long imageId) {

        // Find the Product that was created from this image
        Product product = productRepository.findByImageId(imageId)
                .orElseThrow(() -> new RuntimeException("Listing not found for imageId: " + imageId));

        return buildListingResponse(product);
    }


    // Edit any field in the draft listing
    public ListingResponse editListing(Long productId, Long sellerId, ListingEditRequest request) {

        // Ownership check — seller can only edit their own products
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied"));

        // Update only the fields that are non-null in the request (partial update)
        if (request.getTitle() != null) {
            product.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
            // Seller overriding the AI category — mark it accordingly
            product.setAiSuggestedCategory(false);
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getColor() != null) {
            product.setColor(request.getColor());
        }
        if (request.getMaterial() != null) {
            product.setMaterial(request.getMaterial());
        }
        if (request.getProductType() != null) {
            product.setProductType(request.getProductType());
        }
        if (request.getHighlights() != null) {
            // Convert list back to pipe-separated string for DB storage
            product.setHighlights(String.join("|", request.getHighlights()));
        }
        if (request.getTags() != null) {
            // Replace existing tags with the new set
            tagService.saveTags(productId, request.getTags());
        }

        productRepository.save(product);
        return buildListingResponse(product);
    }


    // Publish the confirmed listing to the marketplace
    public PublishResponse publishListing(Long productId, Long sellerId) {

        // Ownership check
        Product product = productRepository.findByIdAndSellerId(productId, sellerId)
                .orElseThrow(() -> new RuntimeException("Product not found or access denied"));

        // Validate the listing has a price before publishing
        if (product.getPrice() == null) {
            throw new IllegalArgumentException(
                    "Cannot publish listing without a price. Please set a price before publishing.");
        }

        product.setStatus("PUBLISHED");
        productRepository.save(product);

        return PublishResponse.builder()
                .productId(product.getId())
                .status("PUBLISHED")
                .title(product.getTitle())
                .message("Product published successfully to the marketplace.")
                .build();
    }


    // Internal helper — builds ListingResponse from Product
    private ListingResponse buildListingResponse(Product product) {

        // Fetch tags for this product from the tags table
        List<String> tags = tagRepository.findByProductId(product.getId())
                .stream()
                .map(tag -> tag.getTagName())
                .collect(Collectors.toList());

        // Split pipe-separated highlights string back into a list
        List<String> highlights = (product.getHighlights() != null && !product.getHighlights().isBlank())
                ? Arrays.asList(product.getHighlights().split("\\|"))
                : List.of();

        // Fetch the original image URL from the images table
        String originalImageUrl = imageRepository.findById(product.getImageId())
                .map(Image::getOriginalUrl)
                .orElse(null);

        return ListingResponse.builder()
                .productId(product.getId())
                .imageId(product.getImageId())
                .originalImageUrl(originalImageUrl)
                .processedImageUrl(product.getImageUrl())
                .title(product.getTitle())
                .description(product.getDescription())
                .category(product.getCategory())
                .aiSuggestedCategory(product.isAiSuggestedCategory())
                .color(product.getColor())
                .material(product.getMaterial())
                .productType(product.getProductType())
                .price(product.getPrice())
                .suggestedPriceMin(product.getSuggestedPriceMin())
                .suggestedPriceMax(product.getSuggestedPriceMax())
                .tags(tags)
                .highlights(highlights)
                .status(product.getStatus())
                .build();
    }
}