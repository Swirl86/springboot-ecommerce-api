package com.swirl.ecomengine.product.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ForbiddenException;
import com.swirl.ecomengine.product.controller.review.ProductReviewController;
import com.swirl.ecomengine.product.dto.review.ProductReviewRequest;
import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import com.swirl.ecomengine.product.exception.ReviewNotFoundException;
import com.swirl.ecomengine.product.service.review.ProductReviewService;
import com.swirl.ecomengine.security.user.AuthenticatedUserArgumentResolver;
import com.swirl.ecomengine.user.User;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductReviewController.class)
@Import(SecurityTestConfigMinimal.class)
@ActiveProfiles("test-controller")
class ProductReviewControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper json;

    @MockBean private ProductReviewService reviewService;
    @MockBean private AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;

    private ProductReviewRequest validRequest;
    private ProductReviewResponse response1;
    private ProductReviewResponse response2;

    @BeforeEach
    void setup() {
        User testUser = TestDataFactory.user(pwd -> "encoded");
        testUser.setId(5L);

        validRequest = TestDataFactory.productReviewRequest(5, "Great product!");
        response1 = TestDataFactory.newProductReviewResponse(1L, 5, "Great product!", "Alice");
        response2 = TestDataFactory.newProductReviewResponse(2L, 4, "Good", "Bob");

        when(authenticatedUserArgumentResolver.supportsParameter(any()))
                .thenReturn(true);

        when(authenticatedUserArgumentResolver.resolveArgument(any(), any(), any(), any()))
                .thenReturn(testUser);
    }

    // ============================================================
    // POST /products/{id}/reviews
    // ============================================================

    @Test
    void addReview_shouldReturn200_whenReviewIsCreated() throws Exception {
        when(reviewService.addReview(eq(1L), any(User.class), any()))
                .thenReturn(response1);

        mvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Great product!"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastEditedAt").exists());
    }

    @Test
    void addReview_shouldReturn400_whenInvalidRequest() throws Exception {
        ProductReviewRequest invalid = TestDataFactory.productReviewRequest(0, "Bad");

        mvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void addReview_shouldReturn400_whenServiceThrowsBadRequest() throws Exception {
        when(reviewService.addReview(eq(1L), any(User.class), any()))
                .thenThrow(new BadRequestException("User must purchase product before reviewing"));

        mvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User must purchase product before reviewing"));
    }

    // ============================================================
    // GET /products/{id}/reviews
    // ============================================================

    @Test
    void getReviews_shouldReturn200_withListOfReviews() throws Exception {
        when(reviewService.getReviewsForProduct(1L))
                .thenReturn(List.of(response1, response2));

        mvc.perform(get("/products/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating").value(5))
                .andExpect(jsonPath("$[1].rating").value(4))
                .andExpect(jsonPath("$[0].createdAt").exists())
                .andExpect(jsonPath("$[0].lastEditedAt").exists())
                .andExpect(jsonPath("$[1].createdAt").exists())
                .andExpect(jsonPath("$[1].lastEditedAt").exists());
    }

    @Test
    void getReviews_shouldReturn404_whenProductNotFound() throws Exception {
        when(reviewService.getReviewsForProduct(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mvc.perform(get("/products/999/reviews"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id 999 not found"));
    }

    // ============================================================
    // GET /products/{id}/reviews/average
    // ============================================================

    @Test
    void getAverageRating_shouldReturn200_withValue() throws Exception {
        when(reviewService.getAverageRating(1L)).thenReturn(4.5);

        mvc.perform(get("/products/1/reviews/average"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }

    @Test
    void getAverageRating_shouldReturn404_whenProductNotFound() throws Exception {
        when(reviewService.getAverageRating(999L))
                .thenThrow(new ProductNotFoundException(999L));

        mvc.perform(get("/products/999/reviews/average"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product with id 999 not found"));
    }

    // ============================================================
    // PUT /products/{productId}/reviews/{reviewId}
    // ============================================================

    @Test
    void updateReview_shouldReturn200_whenReviewIsUpdated() throws Exception {
        ProductReviewResponse updated = TestDataFactory.newProductReviewResponse(
                1L, 4, "Updated comment", "Alice"
        );

        when(reviewService.updateReview(eq(1L), eq(10L), any(User.class), any()))
                .thenReturn(updated);

        ProductReviewRequest updateRequest =
                TestDataFactory.productReviewRequest(4, "Updated comment");

        mvc.perform(put("/products/1/reviews/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Updated comment"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastEditedAt").exists());
    }

    @Test
    void updateReview_shouldReturn403_whenUserDoesNotOwnReview() throws Exception {
        when(reviewService.updateReview(eq(1L), eq(10L), any(User.class), any()))
                .thenThrow(new ForbiddenException("You can only edit your own review"));

        ProductReviewRequest updateRequest =
                TestDataFactory.productReviewRequest(4, "Updated comment");

        mvc.perform(put("/products/1/reviews/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only edit your own review"));
    }

    @Test
    void updateReview_shouldReturn404_whenReviewNotFound() throws Exception {
        when(reviewService.updateReview(eq(1L), eq(10L), any(User.class), any()))
                .thenThrow(new ReviewNotFoundException());

        ProductReviewRequest updateRequest =
                TestDataFactory.productReviewRequest(4, "Updated comment");

        mvc.perform(put("/products/1/reviews/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Review not found"));
    }


    // ============================================================
    // ERROR HANDLING
    // ============================================================

    @Test
    void shouldReturn500_whenUnexpectedExceptionOccurs() throws Exception {
        when(reviewService.addReview(eq(1L), any(User.class), any()))
                .thenThrow(new RuntimeException("boom"));

        mvc.perform(post("/products/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError());
    }
}
