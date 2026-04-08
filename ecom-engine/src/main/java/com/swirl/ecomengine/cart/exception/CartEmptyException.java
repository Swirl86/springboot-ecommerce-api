package com.swirl.ecomengine.cart.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class CartEmptyException extends BadRequestException {
    public CartEmptyException() {
        super("Cart is empty");
    }
}
