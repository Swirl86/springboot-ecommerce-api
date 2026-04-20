package com.swirl.ecomengine.user;

import com.swirl.ecomengine.address.Address;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // ---------------------------------------------------------
    // PROFILE DATA REQUIRED FOR CHECKOUT
    // OrderService.placeOrder(...) validates name, phone and address.
    // After registration, the user should update their profile.
    // ---------------------------------------------------------
    private String name;

    private String phone;

    // USER ROLE ONLY
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Address address;

    // ---------------------------------------------------------
    // ROLE HELPERS
    // ---------------------------------------------------------
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public boolean isUser() {
        return this.role == Role.USER;
    }
}