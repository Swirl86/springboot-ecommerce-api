package com.swirl.ecomengine.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.category.controller.CategoryController;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class CategoryControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @MockBean private CategoryService categoryService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    // ============================================================
    // GET /categories
    // ============================================================

    @Test
    void getAllCategories_shouldReturn200_withList() throws Exception {
        List<CategoryResponse> categories = List.of(
                new CategoryResponse(1L, "Electronics"),
                new CategoryResponse(2L, "Accessories")
        );

        when(categoryService.getAll()).thenReturn(categories);

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Accessories"));
    }

    @Test
    void getAllCategories_shouldReturn200_withEmptyList() throws Exception {
        when(categoryService.getAll()).thenReturn(List.of());

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ============================================================
    // GET /categories/{id}
    // ============================================================

    @Test
    void getCategoryById_shouldReturn200_whenFound() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Electronics");

        when(categoryService.getCategoryById(1L)).thenReturn(response);

        mvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void getCategoryById_shouldReturn404_whenNotFound() throws Exception {
        when(categoryService.getCategoryById(1L))
                .thenThrow(new CategoryNotFoundException(1L));

        mvc.perform(get("/categories/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Category with id 1 not found"))
                .andExpect(jsonPath("$.path").value("/categories/1"));
    }

    // ============================================================
    // POST /categories
    // ============================================================

    @Test
    void createCategory_shouldReturn201_whenValidRequest() throws Exception {
        CategoryRequest request = new CategoryRequest("Electronics");
        CategoryResponse response = new CategoryResponse(1L, "Electronics");

        when(categoryService.create(any())).thenReturn(response);

        mvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Electronics"));
    }

    @Test
    void createCategory_shouldReturn400_whenInvalidRequest() throws Exception {
        CategoryRequest invalid = new CategoryRequest("");

        mvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // ERROR HANDLING
    // ============================================================

    @Test
    void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
        when(categoryService.getAll()).thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/categories"))
                .andExpect(status().isInternalServerError());
    }
}