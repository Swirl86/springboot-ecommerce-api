package com.swirl.ecomengine.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.category.controller.CategoryController;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.security.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.validation.ValidationAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import({ValidationAutoConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    // ------------------------------------------------------------
    // GET /categories — 200 OK
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getAllCategories_returns200() throws Exception {
        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Electronics"),
                new CategoryResponse(2L, "Accessories")
        );

        when(categoryService.getAll()).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].name").value("Accessories"));
    }

    // ------------------------------------------------------------
    // GET /categories/{id} — 404 Not Found
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getCategory_returns404_whenNotFound() throws Exception {
        when(categoryService.getCategoryById(1L))
                .thenThrow(new CategoryNotFoundException(1L));

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category with id 1 not found"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    // ------------------------------------------------------------
    // POST /categories — 201 Created
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_returns201() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");
        CategoryResponse response = new CategoryResponse(1L, "Electronics");

        when(categoryService.create(any())).thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }
}