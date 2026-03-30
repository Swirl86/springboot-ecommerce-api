package com.swirl.ecomengine.auth.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class JwtValidationException extends BadRequestException {
    public JwtValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
