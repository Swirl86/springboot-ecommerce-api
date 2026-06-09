package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class ReviewNotFoundException extends NotFoundException {
    public ReviewNotFoundException(Long id) {
        super("Review with id " + id + " not found");
    }
    public ReviewNotFoundException() {
        super("Review not found");
    }
}