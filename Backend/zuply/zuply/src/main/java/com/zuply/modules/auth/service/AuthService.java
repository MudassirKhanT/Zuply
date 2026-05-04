package com.zuply.modules.auth.service;

import com.zuply.common.enums.Role;
import com.zuply.modules.auth.dto.LoginRequest;
import com.zuply.modules.auth.dto.LoginResponse;
import com.zuply.modules.auth.dto.RegisterRequest;
import com.zuply.modules.seller.model.Seller;
import com.zuply.modules.seller.repository.SellerRepository;
import com.zuply.modules.user.dto.UserProfileDto;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import com.zuply.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired private UserRepository userRepository;
    @Autowired private SellerRepository sellerRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    public UserProfileDto register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        User saved = userRepository.save(user);

        // Auto-create Seller record so the seller can use the platform immediately
        if (request.getRole() == Role.SELLER) {
            Seller seller = new Seller();
            seller.setUser(saved);
            seller.setStoreName(request.getName() + "'s Store");
            seller.setVerificationStatus("PENDING");
            seller.setActive(false);
            sellerRepository.save(seller);
        }

        return toDto(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getRole().equals(request.getRole())) {
            throw new RuntimeException("Invalid role");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new LoginResponse(token, user.getRole().name(), user.getName(), user.getEmail());
    }

    private UserProfileDto toDto(User user) {
        UserProfileDto dto = new UserProfileDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAddress(user.getAddress());
        dto.setPincode(user.getPincode());
        return dto;
    }
}
