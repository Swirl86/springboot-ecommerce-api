package com.swirl.ecomengine.common.exception;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found");
    }
}
