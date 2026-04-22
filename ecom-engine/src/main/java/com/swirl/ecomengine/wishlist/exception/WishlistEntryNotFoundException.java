package com.swirl.ecomengine.wishlist.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class WishlistEntryNotFoundException extends NotFoundException {
    public WishlistEntryNotFoundException(Long productId, Long userId) {
        super("Product " + productId + " is not in wishlist for user " + userId);
    }
}
