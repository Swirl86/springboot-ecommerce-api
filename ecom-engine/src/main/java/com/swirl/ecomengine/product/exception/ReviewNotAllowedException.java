package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class ReviewNotAllowedException extends BadRequestException {

    public ReviewNotAllowedException(String message) {
        super(message);
    }
}
