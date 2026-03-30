package com.swirl.ecomengine.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.EcomEngineApplication;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.security.SecurityConfig;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.Role;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = {
                EcomEngineApplication.class,
                SecurityConfig.class
        }
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private Long categoryId;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create category
        Category category = new Category(null, "Electronics");
        categoryRepository.save(category);
        categoryId = category.getId();

        // Create ADMIN
        User admin = new User(null, "admin@example.com", passwordEncoder.encode("password"), Role.ADMIN);
        userRepository.save(admin);
        adminToken = jwtService.generateToken(admin);

        // Create USER
        User user = new User(null, "user@example.com", passwordEncoder.encode("password"), Role.USER);
        userRepository.save(user);
        userToken = jwtService.generateToken(user);
    }

    // ============================================================
    // POST /products
    // ============================================================

    @Test
    void adminCanCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void userForbiddenToCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        mvc.perform(post("/products")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotCreateProduct() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ============================================================
    // GET /products/{id}
    // ============================================================

    @Test
    void getProduct_shouldReturn200_whenExists() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Laptop", 999.99, "Powerful laptop", getCategory())
        );

        mvc.perform(get("/products/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void getProduct_shouldReturn404_whenNotFound() throws Exception {
        mvc.perform(get("/products/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // PUT /products/{id}
    // ============================================================

    @Test
    void adminCanUpdateProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Old", 100, "Old desc", getCategory())
        );

        ProductRequest update = new ProductRequest("New", 200, "Updated", categoryId);

        mvc.perform(put("/products/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New"));
    }

    @Test
    void userForbiddenToUpdateProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Old", 100, "Old desc", getCategory())
        );

        ProductRequest update = new ProductRequest("New", 200, "Updated", categoryId);

        mvc.perform(put("/products/" + saved.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(update)))
                .andExpect(status().isForbidden());
    }

    // ============================================================
    // DELETE /products/{id}
    // ============================================================

    @Test
    void adminCanDeleteProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Laptop", 999.99, "desc", getCategory())
        );

        mvc.perform(delete("/products/" + saved.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void userForbiddenToDeleteProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Laptop", 999.99, "desc", getCategory())
        );

        mvc.perform(delete("/products/" + saved.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotDeleteProduct() throws Exception {
        Product saved = productRepository.save(
                new Product(null, "Laptop", 999.99, "desc", getCategory())
        );

        mvc.perform(delete("/products/" + saved.getId()))
                .andExpect(status().isUnauthorized());
    }

    private Category getCategory() {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalStateException("Category missing in test setup"));
    }
}