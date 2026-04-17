package com.swirl.ecomengine.common.exception;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException() {
        super("Email already in use");
    }
}
