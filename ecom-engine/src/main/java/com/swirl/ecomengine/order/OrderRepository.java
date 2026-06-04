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

    // ACTIVE
    List<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses);

    // HISTORY (paginated)
    Page<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses, Pageable pageable);

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

    // ---------------------------------------------------------
    // PURCHASE CHECK (required for allowing product reviews)
    // ---------------------------------------------------------
    boolean existsByUserIdAndStatusInAndItemsProductId(
            Long userId,
            List<OrderStatus> statuses,
            Long productId
    );
}