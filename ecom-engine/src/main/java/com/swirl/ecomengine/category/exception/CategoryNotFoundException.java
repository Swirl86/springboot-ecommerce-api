package com.swirl.ecomengine.category.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class CategoryNotFoundException extends NotFoundException {
    public CategoryNotFoundException(Long id) {
        super("Category with id " + id + " not found");
    }
}
