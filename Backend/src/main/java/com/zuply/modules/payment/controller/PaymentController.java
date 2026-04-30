package com.zuply.modules.payment.controller;

import com.razorpay.RazorpayException;
import com.zuply.modules.payment.dto.PaymentOrderRequest;
import com.zuply.modules.payment.dto.PaymentOrderResponse;
import com.zuply.modules.payment.dto.PaymentVerifyRequest;
import com.zuply.modules.payment.model.Payment;
import com.zuply.modules.payment.service.PaymentService;
import com.zuply.modules.user.model.User;
import com.zuply.modules.user.repository.UserRepository;
import com.zuply.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @RequestBody PaymentOrderRequest request,
            Authentication authentication) {
        try {
            User user = userRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            PaymentOrderResponse response = paymentService.createOrder(request, user.getId());
            return ResponseEntity.ok(ApiResponse.success("Payment order created", response));
        } catch (RazorpayException e) {
            String msg = e.getMessage();
            // Provide a user-friendly message while preserving the raw error for debugging
            String userMsg = (msg != null && msg.contains("The api key provided is invalid"))
                    ? "Payment gateway configuration error. Please use Cash on Delivery."
                    : "Failed to initiate payment: " + msg;
            return ResponseEntity.badRequest().body(ApiResponse.failure(userMsg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.failure("Payment service error: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestBody PaymentVerifyRequest request) {
        boolean isValid = paymentService.verifyPayment(request);
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", "PAID"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.failure("Payment verification failed"));
        }
    }

    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentStatus(@PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success("Payment status fetched", payment));
    }
}
