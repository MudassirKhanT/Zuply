package com.zuply.modules.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public class CheckoutRequest {

    private Long customerId;

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDto deliveryAddress;

    @NotBlank(message = "Payment method must not be blank")
    @Pattern(regexp = "^(ONLINE|COD|UPI|CARD|WALLET)$",
             message = "Payment method must be one of: ONLINE, COD, UPI, CARD, WALLET")
    private String paymentMethod;

    // items is optional — null means "use the customer's cart"
    @Size(max = 50, message = "Order cannot contain more than 50 items")
    private List<@Valid CheckoutItemDto> items;

    public Long getCustomerId()                          { return customerId; }
    public void setCustomerId(Long customerId)           { this.customerId = customerId; }

    public DeliveryAddressDto getDeliveryAddress()       { return deliveryAddress; }
    public void setDeliveryAddress(DeliveryAddressDto d) { this.deliveryAddress = d; }

    public String getPaymentMethod()                     { return paymentMethod; }
    public void setPaymentMethod(String m)               { this.paymentMethod = m; }

    public List<CheckoutItemDto> getItems()              { return items; }
    public void setItems(List<CheckoutItemDto> items)    { this.items = items; }

    public static class CheckoutItemDto {

        @NotNull(message = "Product ID must not be null")
        private Long productId;

        @NotNull(message = "Quantity must not be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        @Max(value = 100, message = "Quantity cannot exceed 100")
        private Integer quantity;

        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        private Double price;

        public Long getProductId()           { return productId; }
        public void setProductId(Long id)    { this.productId = id; }

        public Integer getQuantity()              { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public Double getPrice()             { return price; }
        public void setPrice(Double price)   { this.price = price; }
    }
}
