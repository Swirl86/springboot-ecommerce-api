package com.swirl.ecomengine.auth.exception;

public class JwtValidationException extends RuntimeException {
    public JwtValidationException(Throwable cause) {
        super("Invalid JWT token", cause);
    }
}
