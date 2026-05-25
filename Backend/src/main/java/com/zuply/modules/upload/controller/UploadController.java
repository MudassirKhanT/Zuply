package com.zuply.modules.upload.controller;

import com.zuply.common.ApiResponse;
import com.zuply.modules.upload.dto.UploadResponse;
import com.zuply.modules.upload.service.UploadService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UploadService uploadService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            // TODO 1 — Extract userId from JWT (same pattern as Sprint 1)
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long userId = user.getId();

            // TODO 2 — Call the service
            UploadResponse response = uploadService.uploadImage(file, userId);

            // TODO 3 — Return the response
            return ResponseEntity.ok(new ApiResponse<>(true, "Upload successful", response));

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, ex.getMessage(), null));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(false, "Upload failed: " + ex.getMessage(), null));
        }
    }
}
