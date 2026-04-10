package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderHistoryMapper {

    public OrderHistoryResponse toResponse(OrderHistoryEntry entry) {
        return new OrderHistoryResponse(
                entry.getId(),
                entry.getFromStatus(),
                entry.getToStatus(),
                entry.getChangedAt(),
                entry.getChangedBy().getEmail()
        );
    }
}
