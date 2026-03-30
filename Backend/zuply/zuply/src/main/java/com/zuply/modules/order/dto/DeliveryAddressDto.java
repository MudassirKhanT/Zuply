package com.zuply.modules.order.dto;

import jakarta.validation.constraints.NotBlank;

public class DeliveryAddressDto {

    @NotBlank(message = "Customer name must not be empty")
    private String customerName;

    @NotBlank(message = "Phone must not be empty")
    private String phone;

    @NotBlank(message = "Address must not be empty")
    private String address;

    @NotBlank(message = "City must not be empty")
    private String city;

    @NotBlank(message = "Pincode must not be empty")
    private String pincode;

    public DeliveryAddressDto() {}

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}