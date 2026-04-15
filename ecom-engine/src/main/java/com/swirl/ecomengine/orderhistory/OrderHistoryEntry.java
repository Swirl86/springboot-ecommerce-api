package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderHistoryEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus toStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    /**
     * Optional human-readable explanation for why the status changed.
     * <pre>
     * Examples:
     *  - "Admin approved shipment"
     *  - "User requested cancellation"
     *  - "System auto-completed after delivery scan"
     *  </pre>
     */
    @Column(length = 255)
    private String reason;
}