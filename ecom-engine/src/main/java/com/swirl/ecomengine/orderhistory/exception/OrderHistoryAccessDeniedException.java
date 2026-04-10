package com.swirl.ecomengine.orderhistory.exception;

import com.swirl.ecomengine.common.exception.ForbiddenException;

public class OrderHistoryAccessDeniedException extends ForbiddenException {

    public OrderHistoryAccessDeniedException(Long orderId) {
        super("Not allowed to view history for order: " + orderId);
    }
}