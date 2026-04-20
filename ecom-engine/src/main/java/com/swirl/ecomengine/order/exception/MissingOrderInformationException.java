package com.swirl.ecomengine.order.exception;

public class MissingOrderInformationException extends OrderBadRequestException {
    public MissingOrderInformationException(String message) {
        super(message);
    }
}