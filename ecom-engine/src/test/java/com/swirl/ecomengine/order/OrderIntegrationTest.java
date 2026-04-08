package com.swirl.ecomengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.CartRepository;
import com.swirl.ecomengine.cart.item.CartItem;
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
import org.springframework.test.web.servlet.MvcResult;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String userToken;
    private Product laptop;
    private Product tablet;

    @BeforeEach
    void setup() {
        // ---------------------------------------------------------
        // USER
        // ---------------------------------------------------------
        User user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        // ---------------------------------------------------------
        // CATEGORY
        // ---------------------------------------------------------
        Category category = categoryRepository.save(TestDataFactory.defaultCategory());

        // ---------------------------------------------------------
        // PRODUCTS
        // ---------------------------------------------------------
        laptop = productRepository.save(TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category));
        tablet = productRepository.save(TestDataFactory.product("Tablet", 399.99, "Portable tablet", category));

        // ---------------------------------------------------------
        // CART + ITEMS
        // ---------------------------------------------------------
        Cart cart = new Cart(user);

        CartItem item1 = new CartItem(laptop, 2, laptop.getPrice());
        CartItem item2 = new CartItem(tablet, 1, tablet.getPrice());

        item1.setCart(cart);
        item2.setCart(cart);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // CHECKOUT
    // ---------------------------------------------------------
    @Test
    void checkout_shouldCreateOrderSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(json.get("totalPrice").asDouble())
                .isEqualTo(2 * laptop.getPrice() + tablet.getPrice());

        assertThat(json.get("items").size()).isEqualTo(2);
        assertThat(json.get("status").asText()).isEqualTo(OrderStatus.PENDING.name());
    }
}