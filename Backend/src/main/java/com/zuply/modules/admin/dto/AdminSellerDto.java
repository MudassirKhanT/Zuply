package com.zuply.modules.admin.dto;

import com.zuply.modules.seller.model.Seller;
import lombok.Getter;

@Getter
public class AdminSellerDto {
    private final Long id;
    private final String name;
    private final String email;
    private final String contact;
    private final String storeName;
    private final String location;
    private final String pincode;
    private final String verificationStatus;
    private final boolean active;

    public AdminSellerDto(Seller seller) {
        this.id                 = seller.getId();
        this.name               = seller.getUser() != null ? seller.getUser().getName()  : null;
        this.email              = seller.getUser() != null ? seller.getUser().getEmail() : null;
        this.contact            = seller.getUser() != null ? seller.getUser().getPhone() : null;
        this.storeName          = seller.getStoreName();
        this.location           = seller.getLocation();
        this.pincode            = seller.getPincode();
        this.verificationStatus = seller.getVerificationStatus();
        this.active             = seller.isActive();
    }
}
