package com.swirl.ecomengine.order.exception;

public class OrderAccessDeniedException extends RuntimeException {
    public OrderAccessDeniedException() {
        super("You do not have permission to access this order");
    }
}
