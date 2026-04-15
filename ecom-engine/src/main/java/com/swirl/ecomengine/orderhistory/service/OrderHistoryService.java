package com.swirl.ecomengine.orderhistory.service;

import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.orderhistory.OrderHistoryEntry;
import com.swirl.ecomengine.orderhistory.OrderHistoryRepository;
import com.swirl.ecomengine.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderHistoryService {

    private final OrderHistoryRepository historyRepository;

    public void logStatusChange(Order order, OrderStatus from, OrderStatus to, User changedBy, String reason) {
        OrderHistoryEntry entry = OrderHistoryEntry.builder()
                .order(order)
                .fromStatus(from)
                .toStatus(to)
                .changedAt(LocalDateTime.now())
                .changedBy(changedBy)
                .reason(reason)
                .build();

        historyRepository.save(entry);
    }

    public Page<OrderHistoryEntry> getHistory(Long orderId, Pageable pageable) {
        return historyRepository.findByOrderId(orderId, pageable);
    }
}