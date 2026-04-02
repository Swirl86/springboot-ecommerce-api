package com.swirl.ecomengine.product;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.product.controller.ProductController;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class ProductControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @MockBean private ProductService productService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    // ============================================================
    // GET /products/{id}
    // ============================================================

    @Test
    void getProduct_shouldReturn200_whenProductExists() throws Exception {
        ProductResponse response = new ProductResponse(
                1L, "Laptop", 999.99, "Powerful laptop", 10L, "Electronics"
        );

        when(productService.getProductById(1L)).thenReturn(response);

        mvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.categoryId").value(10))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    void getProduct_shouldReturn404_whenProductNotFound() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new ProductNotFoundException(1L));

        mvc.perform(get("/products/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product with id 1 not found"))
                .andExpect(jsonPath("$.path").value("/products/1"));
    }

    // ============================================================
    // GET /products
    // ============================================================

    @Test
    void getAllProducts_shouldReturn200_withList() throws Exception {
        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "Laptop", 999.99, "Powerful", 10L, "Electronics"),
                new ProductResponse(2L, "Phone", 499.99, "Smartphone", 10L, "Electronics")
        );

        when(productService.getAllProducts()).thenReturn(products);

        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[1].name").value("Phone"));
    }

    // ============================================================
    // POST /products
    // ============================================================

    @Test
    void createProduct_shouldReturn201_whenValidRequest() throws Exception {
        ProductRequest request = new ProductRequest(
                "Laptop", 999.99, "Powerful laptop", 10L
        );

        ProductResponse response = new ProductResponse(
                1L, "Laptop", 999.99, "Powerful laptop", 10L, "Electronics"
        );

        when(productService.createProduct(any())).thenReturn(response);

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.categoryId").value(10))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    void createProduct_shouldReturn400_whenInvalidRequest() throws Exception {
        ProductRequest invalid = new ProductRequest(
                "",   // invalid name
                -10,  // invalid price
                "desc",
                10L
        );

        mvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // PUT /products/{id}
    // ============================================================

    @Test
    void updateProduct_shouldReturn200_whenUpdated() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", 999.99, "desc", 10L);
        ProductResponse response = new ProductResponse(1L, "Laptop", 999.99, "desc", 10L, "Electronics");

        when(productService.updateProduct(eq(1L), any())).thenReturn(response);

        mvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    // ============================================================
    // DELETE /products/{id}
    // ============================================================

    @Test
    void deleteProduct_shouldReturn204_whenDeleted() throws Exception {
        mvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_shouldReturn404_whenNotFound() throws Exception {
        doThrow(new ProductNotFoundException(1L))
                .when(productService).deleteProduct(1L);

        mvc.perform(delete("/products/1"))
                .andExpect(status().isNotFound());
    }

    // ============================================================
    // ERROR HANDLING
    // ============================================================

    @Test
    void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
        when(productService.getProductById(1L))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(get("/products/1"))
                .andExpect(status().isInternalServerError());
    }
}