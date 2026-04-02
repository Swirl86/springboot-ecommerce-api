package com.swirl.ecomengine.cart.service;

import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.CartRepository;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.cart.item.CartItemRepository;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
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
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(new Cart(user)));
    }

    // ---------------------------------------------------------
    // ADD ITEM TO CART
    // ---------------------------------------------------------
    @Transactional
    public Cart addItem(User user, Long productId, int quantity) {
        Cart cart = getOrCreateCart(user);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

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
        Cart cart = getOrCreateCart(user);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Item does not belong to this cart");
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
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));

        cart.removeItem(item);
        cartItemRepository.delete(item);

        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // CLEAR ENTIRE CART
    // ---------------------------------------------------------
    @Transactional
    public Cart clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear(); // orphanRemoval = true handles deletion
        return cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // GET CART (PUBLIC WRAPPER)
    // ---------------------------------------------------------
    public Cart getCart(User user) {
        return getOrCreateCart(user);
    }
}