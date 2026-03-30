package com.zuply.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CheckoutRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDto deliveryAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // UPI, CARD, COD

    // Optional: if null, order is placed from the existing cart
    private List<CheckoutItemDto> items;

    public CheckoutRequest() {}

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public DeliveryAddressDto getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddressDto deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<CheckoutItemDto> getItems() { return items; }
    public void setItems(List<CheckoutItemDto> items) { this.items = items; }

    // Inner class for individual items in checkout payload
    public static class CheckoutItemDto {
        private Long productId;
        private Integer quantity;
        private Double price;

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
    }
}