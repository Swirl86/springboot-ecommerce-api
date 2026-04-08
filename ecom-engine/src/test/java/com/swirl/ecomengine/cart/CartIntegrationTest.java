package com.swirl.ecomengine.cart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.swirl.ecomengine.cart.dto.CartItemRequest;
import com.swirl.ecomengine.cart.dto.CartItemUpdateRequest;
import com.swirl.ecomengine.cart.item.CartItemRepository;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private Product product;

    @BeforeEach
    void setup() {
        // ---------------------------------------------------------
        // Create USER
        // ---------------------------------------------------------
        User user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        // ---------------------------------------------------------
        // Create CATEGORY
        // ---------------------------------------------------------
        Category category = new Category(null, "Electronics");
        category = categoryRepository.save(category);

        // ---------------------------------------------------------
        // Create PRODUCT
        // ---------------------------------------------------------
        product = TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category);
        product = productRepository.save(product);
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private static long extractItemId(String response) {
        Number idNumber = JsonPath.read(response, "$.items[0].id");
        return idNumber.longValue();
    }

    private ResultActions authenticatedPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    // ---------------------------------------------------------
    // ADD ITEM
    // ---------------------------------------------------------
    @Test
    void addItem_shouldAddItemToCart() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 2);

        authenticatedPost("/cart/items", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(product.getId()))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    // ---------------------------------------------------------
    // ADD ITEM (product not found → 404)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldReturnNotFound_whenProductDoesNotExist() throws Exception {
        CartItemRequest request = new CartItemRequest(999L, 2);

        authenticatedPost("/cart/items", request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id 999 not found"));
    }

    // ---------------------------------------------------------
    // ADD ITEM (invalid quantity → 400)
    // ---------------------------------------------------------
    @Test
    void addItem_shouldReturnBadRequest_whenQuantityIsInvalid() throws Exception {
        CartItemRequest request = new CartItemRequest(product.getId(), 0);

        authenticatedPost("/cart/items", request)
                .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------
    // UPDATE ITEM
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldUpdateQuantity() throws Exception {
        // Add item first
        CartItemRequest addRequest = new CartItemRequest(product.getId(), 1);

        String response = authenticatedPost("/cart/items", addRequest)
                .andReturn().getResponse().getContentAsString();

        long itemId = extractItemId(response);

        // Update quantity
        CartItemUpdateRequest updateRequest = new CartItemUpdateRequest(5);

        mockMvc.perform(put("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    // ---------------------------------------------------------
    // UPDATE ITEM (item not found → 404)
    // ---------------------------------------------------------
    @Test
    void updateItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        CartItemUpdateRequest request = new CartItemUpdateRequest(5);

        mockMvc.perform(put("/cart/items/999")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item with id 999 not found"));
    }

    // ---------------------------------------------------------
    // REMOVE ITEM
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldRemoveItemFromCart() throws Exception {
        CartItemRequest addRequest = new CartItemRequest(product.getId(), 1);

        String response = authenticatedPost("/cart/items", addRequest)
                .andReturn().getResponse().getContentAsString();

        long itemId = extractItemId(response);

        mockMvc.perform(delete("/cart/items/" + itemId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---------------------------------------------------------
    // REMOVE ITEM (item not found → 404)
    // ---------------------------------------------------------
    @Test
    void removeItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        mockMvc.perform(delete("/cart/items/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Cart item with id 999 not found"));
    }

    // ---------------------------------------------------------
    // CLEAR CART
    // ---------------------------------------------------------
    @Test
    void clearCart_shouldRemoveAllItems() throws Exception {
        CartItemRequest addRequest = new CartItemRequest(product.getId(), 3);

        authenticatedPost("/cart/items", addRequest)
                .andExpect(status().isOk());

        mockMvc.perform(delete("/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    // ---------------------------------------------------------
    // GET CART
    // ---------------------------------------------------------
    @Test
    void getCart_shouldReturnEmptyCartInitially() throws Exception {
        mockMvc.perform(get("/cart")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }
}