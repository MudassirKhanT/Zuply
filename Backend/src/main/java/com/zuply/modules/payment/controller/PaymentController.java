package com.zuply.modules.payment.controller;

import com.razorpay.RazorpayException;
import com.zuply.modules.payment.dto.PaymentOrderRequest;
import com.zuply.modules.payment.dto.PaymentOrderResponse;
import com.zuply.modules.payment.dto.PaymentVerifyRequest;
import com.zuply.modules.payment.model.Payment;
import com.zuply.modules.payment.service.PaymentService;
import com.zuply.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // Step 1 — Create Razorpay order (CUSTOMER only)
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @RequestBody PaymentOrderRequest request) {

        try {
            Long customerId = getCustomerId();
            PaymentOrderResponse response =
                    paymentService.createOrder(request, customerId);
            return ResponseEntity.ok(
                    ApiResponse.success("Payment order created", response));
        } catch (RazorpayException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.failure("Failed to create payment: "
                            + e.getMessage()));
        }
    }

    // Step 2 — Verify payment after Razorpay callback
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyPayment(
            @RequestBody PaymentVerifyRequest request) {

        boolean isValid = paymentService.verifyPayment(request);

        if (isValid) {
            return ResponseEntity.ok(
                    ApiResponse.success("Payment verified successfully",
                            "PAID"));
        } else {
            return ResponseEntity.badRequest().body(
                    ApiResponse.failure("Payment verification failed"));
        }
    }

    // Get payment status for an order
    @GetMapping("/status/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentStatus(
            @PathVariable Long orderId) {
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(
                ApiResponse.success("Payment status fetched", payment));
    }

    private Long getCustomerId() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        return Long.parseLong(auth.getName());
    }
}