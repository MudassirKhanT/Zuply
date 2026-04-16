package com.zuply.modules.seller.dto;

public class SellerDashboardDto {

    private long totalProductsUploaded;
    private long totalOrdersReceived;
    private long pendingOrders;

    public SellerDashboardDto() {}

    public SellerDashboardDto(long totalProductsUploaded, long totalOrdersReceived, long pendingOrders) {
        this.totalProductsUploaded = totalProductsUploaded;
        this.totalOrdersReceived = totalOrdersReceived;
        this.pendingOrders = pendingOrders;
    }

    public long getTotalProductsUploaded() { return totalProductsUploaded; }
    public void setTotalProductsUploaded(long totalProductsUploaded) { this.totalProductsUploaded = totalProductsUploaded; }

    public long getTotalOrdersReceived() { return totalOrdersReceived; }
    public void setTotalOrdersReceived(long totalOrdersReceived) { this.totalOrdersReceived = totalOrdersReceived; }

    public long getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(long pendingOrders) { this.pendingOrders = pendingOrders; }
}
