package com.swirl.ecomengine.security.user.exception;

public class AuthenticatedUserNotFoundException extends RuntimeException {
    public AuthenticatedUserNotFoundException(String message) {
        super(message);
    }
}
