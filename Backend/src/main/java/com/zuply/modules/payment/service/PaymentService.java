package com.zuply.modules.payment.service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.zuply.modules.payment.dto.PaymentOrderRequest;
import com.zuply.modules.payment.dto.PaymentOrderResponse;
import com.zuply.modules.payment.dto.PaymentVerifyRequest;
import com.zuply.modules.payment.model.Payment;
import com.zuply.modules.payment.model.PaymentStatus;
import com.zuply.modules.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // Step 1 — Create Razorpay order
    public PaymentOrderResponse createOrder(
            PaymentOrderRequest request, Long customerId) throws RazorpayException {

        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        JSONObject options = new JSONObject();
        options.put("amount", (int)(request.getAmount() * 100)); // paise
        options.put("currency", "INR");
        options.put("receipt", "rcpt_" + System.currentTimeMillis());

        com.razorpay.Order razorpayOrder = client.orders.create(options);

        // Save payment record
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .customerId(customerId)
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(request.getAmount())
                .status(PaymentStatus.CREATED)
                .build();

        paymentRepository.save(payment);

        return PaymentOrderResponse.builder()
                .razorpayOrderId(razorpayOrder.get("id"))
                .amount(request.getAmount())
                .currency("INR")
                .keyId(keyId)
                .build();
    }

    // Step 2 — Verify payment signature after success
    public boolean verifyPayment(PaymentVerifyRequest request) {

        try {
            // Generate expected signature
            String data = request.getRazorpayOrderId()
                    + "|" + request.getRazorpayPaymentId();

            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(
                    data.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }

            String expectedSignature = hexString.toString();
            boolean isValid = expectedSignature
                    .equals(request.getRazorpaySignature());

            // Update payment record
            Payment payment = paymentRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() ->
                            new RuntimeException("Payment not found"));

            if (isValid) {
                payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
                payment.setRazorpaySignature(request.getRazorpaySignature());
                payment.setStatus(PaymentStatus.PAID);
            } else {
                payment.setStatus(PaymentStatus.FAILED);
            }

            paymentRepository.save(payment);
            return isValid;

        } catch (Exception e) {
            throw new RuntimeException("Payment verification failed: "
                    + e.getMessage());
        }
    }

    // Get payment status for an order
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found for order: "
                                + orderId));
    }
}