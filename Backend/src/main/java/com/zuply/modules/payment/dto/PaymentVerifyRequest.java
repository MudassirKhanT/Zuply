package com.zuply.modules.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentVerifyRequest {

    @NotBlank(message = "Razorpay order ID must not be blank")
    private String razorpayOrderId;

    @NotBlank(message = "Razorpay payment ID must not be blank")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay signature must not be blank")
    private String razorpaySignature;

    @NotNull(message = "Order ID must not be null")
    private Long orderId;
}
