package com.zuply.modules.seller.dto;

public class SellerOrderDto {

    private Long orderId;
    private String customerName;
    private String productName;
    private int quantity;
    private String orderStatus;

    public SellerOrderDto() {}

    public SellerOrderDto(Long orderId, String customerName, String productName, int quantity, String orderStatus) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.productName = productName;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}
