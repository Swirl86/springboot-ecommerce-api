package com.swirl.ecomengine.user.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }

    public UserNotFoundException() {
        super("User not found");
    }
}
