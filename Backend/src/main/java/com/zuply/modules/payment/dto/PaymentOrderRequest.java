package com.zuply.modules.payment.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PaymentOrderRequest {

    @NotNull(message = "Order ID must not be null")
    private Long orderId;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "1.0", message = "Amount must be at least ₹1")
    private Double amount;
}
