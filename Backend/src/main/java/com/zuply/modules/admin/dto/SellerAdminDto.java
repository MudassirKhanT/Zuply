package com.zuply.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerAdminDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String storeName;
    private String location;
    private String pincode;
    private String verificationStatus;
    private boolean active;
}
