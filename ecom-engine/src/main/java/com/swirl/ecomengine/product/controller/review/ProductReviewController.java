package com.swirl.ecomengine.product.controller.review;

import com.swirl.ecomengine.product.dto.review.ProductReviewRequest;
import com.swirl.ecomengine.product.dto.review.ProductReviewResponse;
import com.swirl.ecomengine.product.service.review.ProductReviewService;
import com.swirl.ecomengine.security.user.AuthenticatedUser;
import com.swirl.ecomengine.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products/{productId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Endpoints for creating and retrieving product reviews")
public class ProductReviewController {

    private final ProductReviewService reviewService;

    // ---------------------------------------------------------
    // CREATE REVIEW
    // ---------------------------------------------------------
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Add a review for a product",
            description = """
                    Creates a new review for a product. 
                    The user must be authenticated and must have purchased the product.
                    A user may only review a product once.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review successfully created"),
            @ApiResponse(responseCode = "400", description = "User has not purchased the product or has already reviewed it"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductReviewResponse addReview(
            @Parameter(description = "ID of the product to review", required = true)
            @PathVariable Long productId,
            @AuthenticatedUser User user,
            @Valid @RequestBody ProductReviewRequest request
    ) {
        return reviewService.addReview(productId, user, request);
    }

    // ---------------------------------------------------------
    // GET ALL REVIEWS FOR A PRODUCT
    // ---------------------------------------------------------
    @GetMapping
    @Operation(
            summary = "Get all reviews for a product",
            description = "Returns a list of all reviews for the specified product, sorted by newest first."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reviews returned successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public List<ProductReviewResponse> getReviews(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId
    ) {
        return reviewService.getReviewsForProduct(productId);
    }

    // ---------------------------------------------------------
    // GET AVERAGE RATING
    // ---------------------------------------------------------
    @GetMapping("/average")
    @Operation(
            summary = "Get average rating for a product",
            description = "Returns the average rating for the specified product. If no reviews exist, returns 0.0."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Average rating returned successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public double getAverageRating(
            @Parameter(description = "ID of the product", required = true)
            @PathVariable Long productId
    ) {
        return reviewService.getAverageRating(productId);
    }
}



