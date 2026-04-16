package com.zuply.modules.auth.dto;

import com.zuply.common.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LoginRequest {

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be a valid format")
    private String email;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @NotNull(message = "Role must be one of CUSTOMER, SELLER, ADMIN")
    private Role role;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
}
