package com.swirl.ecomengine.cart.service;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.CartRepository;
import com.swirl.ecomengine.cart.exception.CartItemNotFoundException;
import com.swirl.ecomengine.cart.exception.CartNotFoundException;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.cart.item.CartItemRepository;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ForbiddenException;
import com.swirl.ecomengine.common.exception.UnauthorizedException;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository
    ) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // ---------------------------------------------------------
    // GET OR CREATE CART
    // ---------------------------------------------------------
    @Transactional
    public Cart getOrCreateCart(User user) {
        if (user == null) {
            throw new UnauthorizedException("User must be authenticated");
        }

        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    // ---------------------------------------------------------
    // ADD ITEM TO CART
    // ---------------------------------------------------------
    @Transactional
    public Cart addItem(User user, Long productId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        CartItem item = new CartItem(product, quantity, product.getPrice());
        cart.addItem(item);

        cartItemRepository.save(item);
        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // UPDATE ITEM QUANTITY
    // ---------------------------------------------------------
    @Transactional
    public Cart updateItem(User user, Long itemId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }

        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Item does not belong to this cart");
        }

        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // REMOVE ITEM FROM CART
    // ---------------------------------------------------------
    @Transactional
    public Cart removeItem(User user, Long itemId) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new CartItemNotFoundException(itemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ForbiddenException("Item does not belong to this cart");
        }

        cart.removeItem(item);
        cartItemRepository.delete(item);

        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // CLEAR ENTIRE CART
    // ---------------------------------------------------------
    @Transactional
    public Cart clearCart(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new CartNotFoundException(user.getId()));
        cart.getItems().clear();
        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // GET CART (PUBLIC WRAPPER)
    // ---------------------------------------------------------
    public Cart getCart(User user) {
        return getOrCreateCart(user);
    }
}