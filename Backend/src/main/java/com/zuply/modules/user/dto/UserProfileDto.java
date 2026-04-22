package com.zuply.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public class UserProfileDto {

    @NotBlank(message = "Name must not be blank")
    private String name;

    private String email;

    @NotBlank(message = "Phone must not be blank")
    private String phone;

    @NotBlank(message = "Address must not be blank")
    private String address;

    @NotBlank(message = "Pincode must not be blank")
    private String pincode;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
}
