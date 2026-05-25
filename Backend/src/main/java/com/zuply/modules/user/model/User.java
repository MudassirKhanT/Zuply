package com.zuply.modules.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zuply.common.enums.Role;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonIgnore   // NEVER expose the BCrypt hash in API responses
    private String password;

    private String phone;
    private String address;
    private String city;       // ADDED — was missing, matches DB schema
    private String pincode;

    @Enumerated(EnumType.STRING)
    private Role role;

    public Long   getId()                  { return id; }
    public void   setId(Long id)           { this.id = id; }

    public String getName()                { return name; }
    public void   setName(String name)     { this.name = name; }

    public String getEmail()               { return email; }
    public void   setEmail(String email)   { this.email = email; }

    public String getPassword()            { return password; }
    public void   setPassword(String pw)   { this.password = pw; }

    public String getPhone()               { return phone; }
    public void   setPhone(String phone)   { this.phone = phone; }

    public String getAddress()             { return address; }
    public void   setAddress(String a)     { this.address = a; }

    public String getCity()                { return city; }
    public void   setCity(String city)     { this.city = city; }

    public String getPincode()             { return pincode; }
    public void   setPincode(String p)     { this.pincode = p; }

    public Role   getRole()                { return role; }
    public void   setRole(Role role)       { this.role = role; }
}
