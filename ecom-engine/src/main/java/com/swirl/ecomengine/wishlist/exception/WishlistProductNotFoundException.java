package com.swirl.ecomengine.wishlist.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class WishlistProductNotFoundException extends NotFoundException {
    public WishlistProductNotFoundException(Long productId) {
        super("Product with ID " + productId + " not found");
    }
}

