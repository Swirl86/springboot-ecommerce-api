package com.swirl.ecomengine.cart.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class CartItemNotFoundException extends NotFoundException {

    public CartItemNotFoundException(Long itemId) {
        super("Cart item with id " + itemId + " not found");
    }
}
