package com.zuply.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CheckoutRequest {

    private Long customerId; // set by OrderController from JWT, never required from client

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDto deliveryAddress;  // ✅ nested DTO, not a String

    @NotBlank(message = "Payment method must not be blank")
    private String paymentMethod;

    private List<CheckoutItemDto> items;

    // Getters & Setters
    public Long getCustomerId()                          { return customerId; }
    public void setCustomerId(Long customerId)           { this.customerId = customerId; }

    public DeliveryAddressDto getDeliveryAddress()       { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddressDto d) { this.deliveryAddress = d; }

    public String getPaymentMethod()                     { return paymentMethod; }
    public void setPaymentMethod(String m)               { this.paymentMethod = m; }

    public List<CheckoutItemDto> getItems()              { return items; }
    public void setItems(List<CheckoutItemDto> items)    { this.items = items; }

    // Inner DTO for cart-based checkout items
    public static class CheckoutItemDto {
        private Long productId;
        private Integer quantity;
        private Double price;

        public Long getProductId()           { return productId; }
        public void setProductId(Long id)    { this.productId = id; }

        public Integer getQuantity()              { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice()             { return price; }
        public void setPrice(Double price)   { this.price = price; }
    }
}