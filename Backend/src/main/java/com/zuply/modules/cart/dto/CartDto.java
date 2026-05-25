package com.zuply.modules.cart.dto;

import java.util.List;

public class CartDto {

    private Long cartId;
    private List<CartItemDto> items;
    private Double grandTotal;

    // ── Nested DTO for each line item ─────────────────────────────────────────
    public static class CartItemDto {
        private Long   itemId;
        private Long   productId;
        private String productName;
        private int    quantity;
        private Double pricePerUnit;
        private Double totalPrice;     // quantity × pricePerUnit

        public Long   getItemId()                        { return itemId; }
        public void   setItemId(Long itemId)             { this.itemId = itemId; }

        public Long   getProductId()                     { return productId; }
        public void   setProductId(Long productId)       { this.productId = productId; }

        public String getProductName()                   { return productName; }
        public void   setProductName(String n)           { this.productName = n; }

        public int    getQuantity()                      { return quantity; }
        public void   setQuantity(int quantity)          { this.quantity = quantity; }

        public Double getPricePerUnit()                  { return pricePerUnit; }
        public void   setPricePerUnit(Double p)          { this.pricePerUnit = p; }

        public Double getTotalPrice()                    { return totalPrice; }
        public void   setTotalPrice(Double t)            { this.totalPrice = t; }
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public Long             getCartId()                     { return cartId; }
    public void             setCartId(Long cartId)          { this.cartId = cartId; }

    public List<CartItemDto> getItems()                     { return items; }
    public void             setItems(List<CartItemDto> i)   { this.items = i; }

    public Double           getGrandTotal()                 { return grandTotal; }
    public void             setGrandTotal(Double t)         { this.grandTotal = t; }
}
