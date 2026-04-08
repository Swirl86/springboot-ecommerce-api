package com.swirl.ecomengine.common.exception;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(message);
    }
}
