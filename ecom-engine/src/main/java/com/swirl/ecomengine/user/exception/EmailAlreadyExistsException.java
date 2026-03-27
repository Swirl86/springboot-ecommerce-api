package com.swirl.ecomengine.user.exception;

import com.swirl.ecomengine.common.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("Email already exists: " + email);
    }
}
