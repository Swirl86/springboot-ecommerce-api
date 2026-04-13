package com.swirl.ecomengine.order;

import com.swirl.ecomengine.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserOrderByCreatedAtDesc(User user);
    List<Order> findByUserAndStatusIn(User user, List<OrderStatus> statuses);
    Page<Order> findByUser(User user, Pageable pageable);
}

