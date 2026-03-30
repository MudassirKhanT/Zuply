package com.zuply.modules.cart.dto;

import java.util.List;

public class CartDto {

    private Long cartId;
    private Long customerId;
    private List<CartItemDto> items;
    private Double grandTotal;

    public CartDto() {}

    public CartDto(Long cartId, Long customerId, List<CartItemDto> items) {
        this.cartId = cartId;
        this.customerId = customerId;
        this.items = items;
        this.grandTotal = items.stream()
                .mapToDouble(CartItemDto::getTotalPrice)
                .sum();
    }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public List<CartItemDto> getItems() { return items; }
    public void setItems(List<CartItemDto> items) { this.items = items; }

    public Double getGrandTotal() { return grandTotal; }
    public void setGrandTotal(Double grandTotal) { this.grandTotal = grandTotal; }
}
