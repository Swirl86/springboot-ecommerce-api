package com.swirl.ecomengine.wishlist.exception;

import com.swirl.ecomengine.common.exception.ForbiddenException;

public class WishlistAccessDeniedException extends ForbiddenException {
    public WishlistAccessDeniedException() {
        super("You are not allowed to access this wishlist");
    }
}