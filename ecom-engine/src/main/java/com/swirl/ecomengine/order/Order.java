package com.swirl.ecomengine.order;

import com.swirl.ecomengine.order.item.OrderItem;
import com.swirl.ecomengine.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")

// NOTE: @Where is deprecated in Hibernate 6, but still fully functional.
// This project does not require advanced filtering or dynamic Hibernate filters.
// If future requirements demand more flexible filtering, replace @Where with @Filter + @FilterDef or @SQLRestriction accordingly.
@SuppressWarnings("deprecation")
@Where(clause = "deleted = false")
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    private double totalPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private boolean deleted = false;
}

