package com.swirl.ecomengine.order.exception;

import com.swirl.ecomengine.common.exception.ForbiddenException;

public class OrderAccessDeniedException extends ForbiddenException {
    public OrderAccessDeniedException() {
        super("You do not have permission to access this order");
    }
}
