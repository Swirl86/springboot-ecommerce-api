package com.swirl.ecomengine.orderhistory;

import com.swirl.ecomengine.orderhistory.dto.OrderHistoryResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderHistoryMapper {

    public OrderHistoryResponse toResponse(OrderHistoryEntry entry) {
        String changedByEmail = entry.getChangedBy() != null
                ? entry.getChangedBy().getEmail()
                : "system";

        return new OrderHistoryResponse(
                entry.getId(),
                entry.getFromStatus(),
                entry.getToStatus(),
                entry.getChangedAt(),
                changedByEmail,
                entry.getReason()
        );
    }
}
