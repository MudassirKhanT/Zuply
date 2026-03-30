package com.zuply.modules.auth.demo;

import com.zuply.common.ApiResponse;
import com.zuply.modules.auth.dto.LoginRequest;
import com.zuply.modules.auth.dto.LoginResponse;
import com.zuply.modules.auth.dto.RegisterRequest;
import com.zuply.modules.auth.service.AuthService;
import com.zuply.modules.user.model.User;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User saved = authService.register(request);
            return ResponseEntity.ok(ApiResponse.success(saved, "User registered successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        }
    }
}
