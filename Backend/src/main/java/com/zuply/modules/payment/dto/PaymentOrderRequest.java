package com.zuply.modules.payment.dto;

import lombok.Data;

@Data
public class PaymentOrderRequest {
    private Long orderId;
    private Double amount;
}