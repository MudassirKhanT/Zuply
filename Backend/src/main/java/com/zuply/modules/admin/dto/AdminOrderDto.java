package com.zuply.modules.admin.dto;

import lombok.*;

/**
 * Lightweight order summary returned to the admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminOrderDto {
    private Long   orderId;
    private String customerName;
    private String customerEmail;
    private Double totalAmount;
    private String status;
    private String paymentMethod;
    private String createdAt;        // ISO string, formatted in service
    private int    itemCount;
    private String deliveryAddress;
    private String city;
    private String pincode;
}
