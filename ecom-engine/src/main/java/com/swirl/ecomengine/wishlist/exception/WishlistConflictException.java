package com.swirl.ecomengine.wishlist.exception;

import com.swirl.ecomengine.common.exception.ConflictException;

public class WishlistConflictException extends ConflictException {
    public WishlistConflictException(String message) {
        super(message);
    }
}