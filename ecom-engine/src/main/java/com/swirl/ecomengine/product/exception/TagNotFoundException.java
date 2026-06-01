package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class TagNotFoundException extends NotFoundException {
    public TagNotFoundException(Long id) {
        super("Tag with id " + id + " not found");
    }
}
