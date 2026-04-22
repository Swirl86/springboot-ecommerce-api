package com.swirl.ecomengine.wishlist.exception;

import com.swirl.ecomengine.common.exception.NotFoundException;

public class WishlistNotFoundException extends NotFoundException {
    public WishlistNotFoundException(Long wishlistItemId) {
        super("Wishlist item with ID " + wishlistItemId + " not found");
    }
}
