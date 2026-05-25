package com.zuply.modules.cart.dto;

import jakarta.validation.constraints.*;

public class CartItemRequest {

    @NotNull(message = "Product ID must not be null")
    private Long productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 100, message = "Quantity cannot exceed 100")
    private int quantity;

    public Long getProductId()               { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public int  getQuantity()                { return quantity; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }
}
