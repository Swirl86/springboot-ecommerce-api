package com.swirl.ecomengine.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.cart.CartRepository;
import com.swirl.ecomengine.cart.item.CartItemRepository;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestSupportConfig;
import testsupport.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityTestSupportConfig.class)
@ActiveProfiles("test-integration")
@Transactional
class SecurityIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Long categoryId;

    @BeforeEach
    void setup() {
        // Create ADMIN
        User admin = userRepository.save(TestDataFactory.admin(passwordEncoder));
        adminToken = jwtService.generateToken(admin);

        // Create USER
        User user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        // Create CATEGORY
        Category category = categoryRepository.save(TestDataFactory.defaultCategory());
        categoryId = category.getId();
    }

    // ============================================================
    // AUTH ENDPOINTS
    // ============================================================

    @Test
    void login_shouldBePublic_andReturn200() throws Exception {
        LoginRequest request = new LoginRequest("admin@example.com", "password123");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn401_whenInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("admin@example.com", "incorrect");

        mvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // AUTHORIZATION: /products (GET)
    // ============================================================

    @Test
    void getProducts_shouldBePublic() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void getProducts_shouldAllowAuthenticatedUser() throws Exception {
        mvc.perform(get("/products")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    // ============================================================
    // AUTHORIZATION: /products (POST)
    // ============================================================

    @Test
    void createProduct_shouldReturn403_forUserRole() throws Exception {
        ProductRequest request = TestDataFactory.productRequest(categoryId);

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_shouldReturn201_forAdminRole() throws Exception {
        ProductRequest request = TestDataFactory.productRequest(categoryId);

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createProduct_shouldReturn401_whenUnauthenticated() throws Exception {
        ProductRequest request = TestDataFactory.productRequest(categoryId);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // TOKEN VALIDATION (must use protected endpoint)
    // ============================================================

    @Test
    void requestShouldReturn401_whenTokenIsInvalid() throws Exception {
        mvc.perform(get("/cart")
                        .header("Authorization", "Bearer invalid.token.value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestShouldReturn401_whenTokenIsMissingBearerPrefix() throws Exception {
        mvc.perform(get("/cart")
                        .header("Authorization", adminToken)) // missing Bearer
                .andExpect(status().isUnauthorized());
    }
}