package com.swirl.ecomengine.wishlist.service;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.wishlist.WishlistItem;
import com.swirl.ecomengine.wishlist.WishlistRepository;
import com.swirl.ecomengine.wishlist.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    // ---------------------------------------------------------
    // GET WISHLIST
    // ---------------------------------------------------------
    public List<WishlistItem> getWishlist(User user) {
        return wishlistRepository.findByUser(user);
    }

    // ---------------------------------------------------------
    // GET SINGLE WISHLIST ITEM
    // ---------------------------------------------------------
    public WishlistItem getWishlistItem(Long wishlistItemId, User user) {
        WishlistItem item = wishlistRepository.findById(wishlistItemId)
                .orElseThrow(() -> new WishlistNotFoundException(wishlistItemId));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new WishlistAccessDeniedException();
        }

        return item;
    }

    // ---------------------------------------------------------
    // ADD TO WISHLIST
    // ---------------------------------------------------------
    public void addToWishlist(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WishlistProductNotFoundException(productId));

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            throw new WishlistConflictException("Product already in wishlist");
        }

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setProduct(product);

        wishlistRepository.save(item);
    }

    // ---------------------------------------------------------
    // REMOVE FROM WISHLIST
    // ---------------------------------------------------------
    public void removeFromWishlist(Long productId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new WishlistProductNotFoundException(productId));

        int deleted = wishlistRepository.deleteByUserAndProduct(user, product);
        if (deleted == 0) {
            throw new  WishlistEntryNotFoundException(productId, user.getId());
        }
    }

    public void clearWishlist(User user) {
        wishlistRepository.deleteAllByUser(user);
    }
}
