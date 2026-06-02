package com.swirl.ecomengine.product.controller.tag;

import com.swirl.ecomengine.product.dto.tag.ProductTagRequest;
import com.swirl.ecomengine.product.dto.tag.ProductTagResponse;
import com.swirl.ecomengine.product.service.tag.ProductTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product Tags", description = "Manage tags assigned to a specific product")
@RestController
@RequestMapping("/api/products/{productId}/tags")
@RequiredArgsConstructor
public class ProductTagController {

    private final ProductTagService tagService;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Add a tag to a product",
            description = "Creates and assigns a new tag to the specified product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid tag data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PostMapping
    public ProductTagResponse addTag(
            @PathVariable Long productId,
            @Valid @RequestBody ProductTagRequest request
    ) {
        return tagService.addTagToProduct(productId, request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all tags for a product",
            description = "Returns all tags currently assigned to the specified product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping
    public List<ProductTagResponse> getTags(@PathVariable Long productId) {
        return tagService.getTagsForProduct(productId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get a specific tag for a product",
            description = "Returns a single tag assigned to the product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Tag or product not found")
    })
    @GetMapping("/{tagId}")
    public ProductTagResponse getTag(
            @PathVariable Long productId,
            @PathVariable Long tagId
    ) {
        return tagService.getTag(productId, tagId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Remove a tag from a product",
            description = "Deletes the specified tag from the product."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag removed successfully"),
            @ApiResponse(responseCode = "404", description = "Tag or product not found")
    })
    @DeleteMapping("/{tagId}")
    public void removeTag(
            @PathVariable Long productId,
            @PathVariable Long tagId
    ) {
        tagService.removeTagFromProduct(productId, tagId);
    }
}