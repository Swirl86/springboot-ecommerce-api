package com.swirl.ecomengine.orderhistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntry, Long> {
    List<OrderHistoryEntry> findByOrderIdOrderByChangedAtAsc(Long orderId);
}