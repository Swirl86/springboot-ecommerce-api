package com.swirl.ecomengine.wishlist;

import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.wishlist.exception.WishlistConflictException;
import com.swirl.ecomengine.wishlist.exception.WishlistEntryNotFoundException;
import com.swirl.ecomengine.wishlist.exception.WishlistNotFoundException;
import com.swirl.ecomengine.wishlist.exception.WishlistProductNotFoundException;
import com.swirl.ecomengine.wishlist.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import testsupport.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WishlistServiceTest {

    private WishlistRepository wishlistRepository;
    private ProductRepository productRepository;
    private WishlistService wishlistService;

    private User user;
    private Product product;

    @BeforeEach
    void setup() {
        wishlistRepository = mock(WishlistRepository.class);
        productRepository = mock(ProductRepository.class);

        wishlistService = new WishlistService(wishlistRepository, productRepository);

        user = TestDataFactory.user(pwd -> "encoded");
        user.setId(1L);

        product = TestDataFactory.defaultProduct(TestDataFactory.defaultCategory());
        product.setId(10L);
        product.getCategory().setId(50L);
    }

    // ---------------------------------------------------------
    // GET WISHLIST
    // ---------------------------------------------------------
    @Test
    void getWishlist_returnsItems() {
        WishlistItem item = new WishlistItem();
        item.setId(100L);
        item.setProduct(product);
        item.setUser(user);

        when(wishlistRepository.findByUser(user)).thenReturn(List.of(item));

        var result = wishlistService.getWishlist(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(100L);
    }

    // ---------------------------------------------------------
    // ADD TO WISHLIST
    // ---------------------------------------------------------
    @Test
    void addToWishlist_savesItem() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(false);

        wishlistService.addToWishlist(10L, user);

        ArgumentCaptor<WishlistItem> captor = ArgumentCaptor.forClass(WishlistItem.class);
        verify(wishlistRepository).save(captor.capture());

        assertThat(captor.getValue().getProduct()).isEqualTo(product);
        assertThat(captor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void addToWishlist_throws_whenProductMissing() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.addToWishlist(10L, user))
                .isInstanceOf(WishlistProductNotFoundException.class);
    }

    @Test
    void addToWishlist_throws_whenAlreadyExists() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserAndProduct(user, product)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addToWishlist(10L, user))
                .isInstanceOf(WishlistConflictException.class);
    }

    // ---------------------------------------------------------
    // REMOVE FROM WISHLIST
    // ---------------------------------------------------------
    @Test
    void removeFromWishlist_deletesItem() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.deleteByUserAndProduct(user, product)).thenReturn(1);

        wishlistService.removeFromWishlist(10L, user);

        verify(wishlistRepository).deleteByUserAndProduct(user, product);
    }

    @Test
    void removeFromWishlist_throws_whenProductMissing() {
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(10L, user))
                .isInstanceOf(WishlistProductNotFoundException.class);
    }

    @Test
    void removeFromWishlist_throws_whenNotInWishlist() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(wishlistRepository.deleteByUserAndProduct(user, product)).thenReturn(0);

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(10L, user))
                .isInstanceOf(WishlistEntryNotFoundException.class);
    }

    // ---------------------------------------------------------
    // CLEAR WISHLIST
    // ---------------------------------------------------------
    @Test
    void clearWishlist_deletesAll() {
        wishlistService.clearWishlist(user);

        verify(wishlistRepository).deleteAllByUser(user);
    }
}