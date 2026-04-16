package com.zuply.modules.cart.model;

import com.zuply.modules.user.model.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();   // initialised — never null

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public User getCustomer()                   { return customer; }
    public void setCustomer(User customer)      { this.customer = customer; }

    public List<CartItem> getItems()            { return items; }
    public void setItems(List<CartItem> items)  { this.items = items; }
}
