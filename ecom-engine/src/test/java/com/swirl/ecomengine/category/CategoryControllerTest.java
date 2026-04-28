package com.swirl.ecomengine.category;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.category.controller.CategoryController;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.common.etag.EtagService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class CategoryControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @MockBean private CategoryService categoryService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    @MockBean private EtagService etagService;

    private final LocalDateTime now = LocalDateTime.now();

    // ============================================================
    // GET /categories (paginated)
    // ============================================================

    @Test
    void getAllCategories_shouldReturn200_withPaginatedList() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CategoryResponse> page = new PageImpl<>(
                List.of(
                        new CategoryResponse(1L, "Electronics", now),
                        new CategoryResponse(2L, "Accessories", now)
                ),
                pageable,
                2
        );

        when(categoryService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Electronics"))
                .andExpect(jsonPath("$.content[1].name").value("Accessories"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void getAllCategories_shouldReturn200_withEmptyPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CategoryResponse> emptyPage = Page.empty(pageable);

        when(categoryService.getAll(any(Pageable.class))).thenReturn(emptyPage);

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0));
    }

    @Test
    void getAllCategories_shouldRespectPageAndSizeParameters() throws Exception {
        Pageable pageable = PageRequest.of(2, 5);
        Page<CategoryResponse> page = new PageImpl<>(
                List.of(new CategoryResponse(11L, "Gaming", now)),
                pageable,
                11
        );

        when(categoryService.getAll(any(Pageable.class))).thenReturn(page);

        mvc.perform(get("/categories?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Gaming"))
                .andExpect(jsonPath("$.totalElements").value(11))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.pageable.pageNumber").value(2))
                .andExpect(jsonPath("$.pageable.pageSize").value(5));
    }

    // ============================================================
    // GET /categories/{id}
    // ============================================================

    @Test
    void getCategoryById_shouldReturn200_whenFound() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Electronics", now);

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
        CategoryResponse response = new CategoryResponse(1L, "Electronics", now);

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
        when(categoryService.getAll(any(Pageable.class)))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/categories"))
                .andExpect(status().isInternalServerError());
    }

    // ============================================================
    // ETAG
    // ============================================================

    @Test
    void getAllCategories_shouldReturn304_whenEtagMatches() throws Exception {
        String etag = "\"2024-01-01T10:00:00\"";

        when(categoryService.getLastUpdated()).thenReturn(LocalDateTime.parse("2024-01-01T10:00:00"));
        when(etagService.generate(any())).thenReturn(etag);
        when(etagService.matches(eq(etag), eq(etag))).thenReturn(true);

        mvc.perform(get("/categories")
                        .header("If-None-Match", etag))
                .andExpect(status().isNotModified())
                .andExpect(header().string("ETag", etag));
    }

    @Test
    void getAllCategories_shouldReturn200_andEtag_whenEtagDoesNotMatch() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        Page<CategoryResponse> page = new PageImpl<>(
                List.of(new CategoryResponse(1L, "Electronics", now)),
                pageable,
                1
        );

        String etag = "\"2024-01-01T10:00:00\"";

        when(categoryService.getLastUpdated()).thenReturn(LocalDateTime.parse("2024-01-01T10:00:00"));
        when(etagService.generate(any())).thenReturn(etag);
        when(etagService.matches(any(), any())).thenReturn(false);
        when(categoryService.getAll(any())).thenReturn(page);

        mvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(header().string("ETag", etag))
                .andExpect(jsonPath("$.content.length()").value(1));
    }
}