package com.swirl.ecomengine.product.tag;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.common.etag.EtagService;
import com.swirl.ecomengine.product.controller.tag.ProductTagController;
import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import com.swirl.ecomengine.product.exception.TagDoesNotBelongToProductException;
import com.swirl.ecomengine.product.exception.TagNotFoundException;
import com.swirl.ecomengine.product.service.tag.ProductTagService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import testsupport.SecurityTestConfigMinimal;
import testsupport.TestDataFactory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductTagController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class ProductTagControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @MockBean private ProductTagService tagService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    @MockBean private EtagService etagService;

    private ProductTagRequest request;
    private ProductTagResponse response;

    @BeforeEach
    void setup() {
        request = TestDataFactory.productTagRequest(
                TagType.SALE,
                "Extra price",
                null,
                null,
                null,
                null
        );
        response = TestDataFactory.productTagResponse(10L, TagType.SALE, "Extra price");
    }

    // ============================================================
    // POST /products/{id}/tags
    // ============================================================

    @Test
    void addTag_shouldReturn200_whenTagIsCreated() throws Exception {
        when(tagService.addTagToProduct(eq(1L), any())).thenReturn(response);

        mvc.perform(post("/api/products/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.type").value("SALE"))
                .andExpect(jsonPath("$.label").value("Extra price"));
    }

    @Test
    void addTag_shouldReturn400_whenInvalidRequest() throws Exception {
        ProductTagRequest invalid = TestDataFactory.productTagRequest(
                null, null, null, null, null, null
        );

        mvc.perform(post("/api/products/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ============================================================
    // DELETE /products/{id}/tags/{tagId}
    // ============================================================

    @Test
    void removeTag_shouldReturn200_whenTagIsRemoved() throws Exception {
        mvc.perform(delete("/api/products/1/tags/10"))
                .andExpect(status().isOk());
    }

    @Test
    void removeTag_shouldReturn404_whenTagNotFound() throws Exception {
        doThrow(new TagNotFoundException(10L))
                .when(tagService).removeTagFromProduct(1L, 10L);

        mvc.perform(delete("/api/products/1/tags/10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Tag with id 10 not found"));
    }

    @Test
    void removeTag_shouldReturn400_whenTagDoesNotBelongToProduct() throws Exception {
        doThrow(new TagDoesNotBelongToProductException(1L, 10L))
                .when(tagService).removeTagFromProduct(1L, 10L);

        mvc.perform(delete("/api/products/1/tags/10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Tag with id 10 does not belong to product with id 1"));
    }

    // ============================================================
    // ERROR HANDLING
    // ============================================================

    @Test
    void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
        when(tagService.addTagToProduct(eq(1L), any()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/api/products/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }
}
