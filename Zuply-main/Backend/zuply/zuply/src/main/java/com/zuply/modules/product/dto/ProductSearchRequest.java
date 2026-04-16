package com.zuply.modules.product.dto;

public class ProductSearchRequest {

    private String name;
    private String pincode;
    private String sortBy;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
}