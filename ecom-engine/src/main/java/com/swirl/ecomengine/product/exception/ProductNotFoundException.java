package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(Long id) {
        super("Product with id " + id + " not found");
    }
}
