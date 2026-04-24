package com.zuply.modules.order.dto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderDto {

    private Long          orderId;
    private String        status;
    private Double        totalAmount;
    private String        deliveryAddress;
    private String        city;
    private String        pincode;
    private String        paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemDto> items;

    // ── Nested DTO ────────────────────────────────────────────────────────────
    public static class OrderItemDto {
        private Long   productId;
        private String productName;
        private int    quantity;
        private Double price;
        private Double lineTotal;

        public Long   getProductId()               { return productId; }
        public void   setProductId(Long id)        { this.productId = id; }

        public String getProductName()             { return productName; }
        public void   setProductName(String n)     { this.productName = n; }

        public int    getQuantity()                { return quantity; }
        public void   setQuantity(int q)           { this.quantity = q; }

        public Double getPrice()                   { return price; }
        public void   setPrice(Double p)           { this.price = p; }

        public Double getLineTotal()               { return lineTotal; }
        public void   setLineTotal(Double t)       { this.lineTotal = t; }
    }

    // ── Getters / setters ─────────────────────────────────────────────────────

    public Long   getOrderId()                          { return orderId; }
    public void   setOrderId(Long id)                   { this.orderId = id; }

    public String getStatus()                           { return status; }
    public void   setStatus(String status)              { this.status = status; }

    public Double getTotalAmount()                      { return totalAmount; }
    public void   setTotalAmount(Double t)              { this.totalAmount = t; }

    public String getDeliveryAddress()                  { return deliveryAddress; }
    public void   setDeliveryAddress(String a)          { this.deliveryAddress = a; }

    public String getCity()                             { return city; }
    public void   setCity(String city)                  { this.city = city; }

    public String getPincode()                          { return pincode; }
    public void   setPincode(String p)                  { this.pincode = p; }

    public String getPaymentMethod()                    { return paymentMethod; }
    public void   setPaymentMethod(String m)            { this.paymentMethod = m; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void          setCreatedAt(LocalDateTime t)  { this.createdAt = t; }

    public List<OrderItemDto> getItems()                { return items; }
    public void               setItems(List<OrderItemDto> i) { this.items = i; }
}
