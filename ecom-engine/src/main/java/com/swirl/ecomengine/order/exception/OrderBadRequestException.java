package com.swirl.ecomengine.order.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class OrderBadRequestException extends BadRequestException {
    public OrderBadRequestException(String message) {
        super(message);
    }
}
