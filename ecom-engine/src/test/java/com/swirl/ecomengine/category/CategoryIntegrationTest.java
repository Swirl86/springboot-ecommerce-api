package com.swirl.ecomengine.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.category.dto.CategoryRequest;
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
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryIntegrationTest extends IntegrationTestBase {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String userToken;
    private CategoryRequest request;

    @BeforeEach
    void setup() {
        // Create ADMIN
        User admin = userRepository.save(TestDataFactory.admin(passwordEncoder));
        adminToken = jwtService.generateToken(admin);

        // Create USER
        User user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        request = new CategoryRequest("Electronics");
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
        mvc.perform(post("/categories")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedCannotCreateCategory() throws Exception {
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
    // GET /categories (paginated)
    // ============================================================

    @Test
    void userCanGetCategories_paginated() throws Exception {
        categoryRepository.save(TestDataFactory.category("Electronics"));
        categoryRepository.save(TestDataFactory.category("Accessories"));

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[*].name").value(
                        org.hamcrest.Matchers.containsInAnyOrder("Electronics", "Accessories")
                ))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void getAllCategories_shouldReturnEmptyPage_whenNoneExist() throws Exception {
        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void getAllCategories_shouldRespectPageAndSizeParameters() throws Exception {
        // Create 11 categories
        for (int i = 1; i <= 11; i++) {
            categoryRepository.save(TestDataFactory.category("C" + i));
        }

        mvc.perform(get("/categories?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1)) // page 2 contains only item 11
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.pageable.pageNumber").value(2))
                .andExpect(jsonPath("$.pageable.pageSize").value(5));
    }

    // ============================================================
    // GET /categories/{id}
    // ============================================================

    @Test
    void userCanGetCategoryById() throws Exception {
        Category saved = categoryRepository.save(TestDataFactory.category("Electronics"));

        mvc.perform(get("/categories/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategory_notFound_returns404() throws Exception {
        mvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }
}