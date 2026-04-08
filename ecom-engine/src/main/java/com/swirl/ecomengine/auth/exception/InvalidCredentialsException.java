package com.swirl.ecomengine.auth.exception;

import com.swirl.ecomengine.common.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
