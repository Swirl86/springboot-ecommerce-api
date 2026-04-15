package com.swirl.ecomengine.orderhistory.dto;

import com.swirl.ecomengine.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderHistoryResponse(
        Long id,
        OrderStatus fromStatus,
        OrderStatus toStatus,
        LocalDateTime changedAt,
        String changedByEmail,
        String reason
) {}

