package com.swirl.ecomengine.product.exception;

import com.swirl.ecomengine.common.exception.BadRequestException;

public class TagDoesNotBelongToProductException extends BadRequestException {
    public TagDoesNotBelongToProductException(Long productId, Long tagId) {
        super("Tag with id " + tagId + " does not belong to product with id " + productId);
    }
}
