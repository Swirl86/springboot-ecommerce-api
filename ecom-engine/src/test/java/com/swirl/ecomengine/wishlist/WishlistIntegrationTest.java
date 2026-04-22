package com.swirl.ecomengine.wishlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WishlistIntegrationTest extends IntegrationTestBase {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private User user;
    private String userToken;
    private Product product;
    private Category category;

    @BeforeEach
    void setup() {
        wishlistRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        category = categoryRepository.save(TestDataFactory.defaultCategory());
        product = productRepository.save(TestDataFactory.defaultProduct(category));
    }

    // ---------------------------------------------------------
    // GET /wishlist
    // ---------------------------------------------------------
    @Test
    void getWishlist_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/wishlist")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    // ---------------------------------------------------------
    // POST /wishlist/{productId}
    // ---------------------------------------------------------
    @Test
    void addToWishlist_shouldAddSuccessfully() throws Exception {
        mockMvc.perform(post("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        assertThat(wishlistRepository.findByUser(user)).hasSize(1);
    }

    @Test
    void addToWishlist_shouldReturn404_whenProductNotFound() throws Exception {
        mockMvc.perform(post("/wishlist/9999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void addToWishlist_shouldReturn409_whenAlreadyExists() throws Exception {
        // Add once
        mockMvc.perform(post("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Add again
        mockMvc.perform(post("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    // ---------------------------------------------------------
    // DELETE /wishlist/{productId}
    // ---------------------------------------------------------
    @Test
    void removeFromWishlist_shouldRemoveSuccessfully() throws Exception {
        // Add first
        mockMvc.perform(post("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Remove
        mockMvc.perform(delete("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        assertThat(wishlistRepository.findByUser(user)).isEmpty();
    }

    @Test
    void removeFromWishlist_shouldReturn404_whenNotInWishlist() throws Exception {
        mockMvc.perform(delete("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------
    // DELETE /wishlist
    // ---------------------------------------------------------
    @Test
    void clearWishlist_shouldRemoveAllItems() throws Exception {
        // Add two items
        mockMvc.perform(post("/wishlist/" + product.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        Product p2 = productRepository.save(
                TestDataFactory.product("Tablet", 399.99, "Portable tablet", category)
        );

        mockMvc.perform(post("/wishlist/" + p2.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        // Clear
        mockMvc.perform(delete("/wishlist")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNoContent());

        assertThat(wishlistRepository.findByUser(user)).isEmpty();
    }

    // ---------------------------------------------------------
    // SECURITY
    // ---------------------------------------------------------
    @Test
    void shouldReturn401_whenNoTokenProvided() throws Exception {
        mockMvc.perform(get("/wishlist"))
                .andExpect(status().isUnauthorized());
    }
}