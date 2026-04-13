package com.swirl.ecomengine.orderhistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface OrderHistoryRepository extends JpaRepository<OrderHistoryEntry, Long> {
    Page<OrderHistoryEntry> findByOrderId(Long orderId, Pageable pageable);
}