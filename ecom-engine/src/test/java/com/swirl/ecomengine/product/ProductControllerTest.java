package com.swirl.ecomengine.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.product.controller.ProductController;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import({ValidationAutoConfiguration.class, TestSecurityConfig.class})
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    // ------------------------------------------------------------
    // GET /products/{id} — 200 OK
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getProduct_returns200() throws Exception {
        ProductResponse response =
                new ProductResponse(1L, "Laptop", 999.99, "Powerful laptop", 10L, "Electronics");

        when(productService.getProductById(1L)).thenReturn(response);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    // ------------------------------------------------------------
    // GET /products/{id} — 404 Not Found
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "USER")
    void getProduct_returns404_whenNotFound() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new ProductNotFoundException(1L));

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product with id 1 not found"))
                .andExpect(jsonPath("$.path").value("/products/1"));
    }

    // ------------------------------------------------------------
    // POST /products — 201 Created
    // ------------------------------------------------------------
    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_returns201() throws Exception {
        ProductRequest request =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", 10L);

        ProductResponse response =
                new ProductResponse(1L, "Laptop", 999.99, "Powerful laptop", 10L, "Electronics");

        when(productService.createProduct(any())).thenReturn(response);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.categoryId").value(10L));
    }
}