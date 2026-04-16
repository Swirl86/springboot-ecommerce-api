package com.swirl.ecomengine.orderhistory.dto;

import java.util.List;

public record OrderTimelineResponse(
        Long orderId,
        List<OrderHistoryResponse> events
) {}