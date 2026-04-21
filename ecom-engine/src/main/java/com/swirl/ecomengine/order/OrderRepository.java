package com.swirl.ecomengine.order;

import com.swirl.ecomengine.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // ---------------------------------------------------------
    // USER ORDERS
    // ---------------------------------------------------------
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    List<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses);

    Page<Order> findByUser(User user, Pageable pageable);

    // ---------------------------------------------------------
    // PROFILE SUPPORT
    // ---------------------------------------------------------
    int countByUserId(Long userId);
    int countByUser(User user);

    // ---------------------------------------------------------
    // ADMIN SUPPORT
    // ---------------------------------------------------------
    List<Order> findByStatus(OrderStatus status);

    // ---------------------------------------------------------
    // ARCHIVED ORDERS (soft-delete)
    // ---------------------------------------------------------
    @Query(value = "SELECT * FROM orders WHERE deleted = true", nativeQuery = true)
    List<Order> findArchived();
}