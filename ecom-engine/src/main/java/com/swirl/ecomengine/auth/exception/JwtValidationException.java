package com.swirl.ecomengine.auth.exception;

import com.swirl.ecomengine.common.exception.UnauthorizedException;

public class JwtValidationException extends UnauthorizedException {
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
