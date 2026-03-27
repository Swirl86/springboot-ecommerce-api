package com.swirl.ecomengine.auth.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class InvalidCredentialsException extends BadRequestException {
    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
