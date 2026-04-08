package com.swirl.ecomengine.common.exception;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
