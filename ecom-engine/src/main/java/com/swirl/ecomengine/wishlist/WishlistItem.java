package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Product product;

    private LocalDateTime createdAt = LocalDateTime.now();
}
