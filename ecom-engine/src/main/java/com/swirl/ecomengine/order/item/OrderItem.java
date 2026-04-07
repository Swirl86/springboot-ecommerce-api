package com.swirl.ecomengine.order.item;

import com.swirl.ecomengine.order.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class OrderItem {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private Order order;

    private Long productId;
    private String productName;
    private double price;
    private int quantity;
}

