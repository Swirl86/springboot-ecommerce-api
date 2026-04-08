package com.swirl.ecomengine.cart.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class CartNotFoundException extends NotFoundException {
    public CartNotFoundException(Long id) {
        super("Cart with id " + id + " not found");
    }
}
