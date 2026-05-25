package com.zuply.modules.user.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.user.dto.UserProfileDto;
import com.zuply.modules.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserProfileDto profile = userService.getProfile(email);
            return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", profile));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileDto dto) {
        try {
            String email = authentication.getName();
            UserProfileDto updated = userService.updateProfile(email, dto);
            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(ApiResponse.failure(e.getMessage()));
        }
    }
}
