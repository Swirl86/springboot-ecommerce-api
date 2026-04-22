package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class ProductCategoryMismatchException extends BadRequestException {
    public ProductCategoryMismatchException(Long categoryId) {
        super("Category with id " + categoryId + " is not valid for this product");
    }
}
