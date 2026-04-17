package com.swirl.ecomengine.address.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class AddressNotFoundException extends NotFoundException {
    public AddressNotFoundException(Long userId) {
        super("No address found for user " + userId);
    }
}