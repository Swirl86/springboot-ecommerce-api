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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import testsupport.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test-integration")
class CartIntegrationTest {

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
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

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