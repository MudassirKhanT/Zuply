package com.zuply.modules.listing.controller;

import com.zuply.common.ApiResponse;
import com.zuply.modules.listing.dto.ListingEditRequest;
import com.zuply.modules.listing.dto.ListingResponse;
import com.zuply.modules.listing.dto.PublishResponse;
import com.zuply.modules.listing.service.ListingService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/listing")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;
    private final UserRepository userRepository;

    // -------------------------------------------------------
    // POST /api/listing/generate/{imageId}
    // Triggers full pipeline: process image → AI → assemble listing
    // -------------------------------------------------------
    @PostMapping("/generate/{imageId}")
    public ResponseEntity<ApiResponse<ListingResponse>> generateListing(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long sellerId = getSellerIdFromUserDetails(userDetails);
            ListingResponse response = listingService.generateListing(imageId, sellerId);
            return ResponseEntity.ok(
                    ApiResponse.success("Listing generated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(ApiResponse.failure("Pipeline failed: " + e.getMessage()));
        }
    }

    // -------------------------------------------------------
    // GET /api/listing/{imageId}
    // Returns the current listing draft for seller preview
    // -------------------------------------------------------
    @GetMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ListingResponse>> getListing(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            ListingResponse response = listingService.getListing(imageId);
            return ResponseEntity.ok(
                    ApiResponse.success("Listing fetched successfully", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    // -------------------------------------------------------
    // PUT /api/listing/{productId}
    // Seller edits any field in the draft listing
    // -------------------------------------------------------
    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ListingResponse>> editListing(
            @PathVariable Long productId,
            @RequestBody ListingEditRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long sellerId = getSellerIdFromUserDetails(userDetails);
            ListingResponse response = listingService.editListing(productId, sellerId, request);
            return ResponseEntity.ok(
                    ApiResponse.success("Listing updated successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    // -------------------------------------------------------
    // POST /api/listing/{productId}/publish
    // Seller confirms and publishes listing to marketplace
    // -------------------------------------------------------
    @PostMapping("/{productId}/publish")
    public ResponseEntity<ApiResponse<PublishResponse>> publishListing(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            Long sellerId = getSellerIdFromUserDetails(userDetails);
            PublishResponse response = listingService.publishListing(productId, sellerId);
            return ResponseEntity.ok(
                    ApiResponse.success("Product published successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.failure(e.getMessage()));
        }
    }

    // -------------------------------------------------------
    // Helper — resolve sellerId from JWT-authenticated user
    // -------------------------------------------------------
    // The JWT stores the seller's email as the principal name.
    // We look up the User record by email to get the numeric id.
    private Long getSellerIdFromUserDetails(UserDetails userDetails) {
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + email));
        return user.getId();
    }
}