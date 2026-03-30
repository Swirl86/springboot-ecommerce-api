package com.swirl.ecomengine.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.EcomEngineApplication;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.product.ProductRepository;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class CategoryIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

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
    // POST /categories
    // ============================================================

    @Test
    void adminCanCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");

        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void userForbiddenToCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");

        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotCreateCategory() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");

        mvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCategory_shouldReturn400_whenInvalidRequest() throws Exception {
        CategoryRequest invalid = new CategoryRequest("");

        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // GET /categories
    // ============================================================

    @Test
    void userCanGetCategories() throws Exception {
        categoryRepository.save(new Category(null, "Electronics"));

        mvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"));
    }

    @Test
    void getAllCategories_shouldReturnEmptyList_whenNoneExist() throws Exception {
        mvc.perform(get("/categories")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ============================================================
    // GET /categories/{id}
    // ============================================================

    @Test
    void userCanGetCategoryById() throws Exception {
        Category saved = categoryRepository.save(new Category(null, "Electronics"));

        mvc.perform(get("/categories/" + saved.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategory_notFound_returns404() throws Exception {
        mvc.perform(get("/categories/999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }
}