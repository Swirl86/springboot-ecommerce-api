package com.swirl.ecomengine.common.exception;

public class UserAccessDeniedException extends RuntimeException {

    public UserAccessDeniedException(String message) {
        super(message);
    }

    public UserAccessDeniedException() {
        super("You do not have permission to perform this action.");
    }

}
