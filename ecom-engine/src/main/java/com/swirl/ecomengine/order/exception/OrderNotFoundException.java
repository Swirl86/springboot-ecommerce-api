package com.swirl.ecomengine.order.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(Long id) {
        super("Order with ID " + id + " not found");
    }
}
