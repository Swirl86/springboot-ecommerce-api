package com.swirl.ecomengine.product.controller;

import com.swirl.ecomengine.product.ProductService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Products", description = "Operations related to product management")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    @Operation(summary = "Get all products", description = "Returns all products with category information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Operation(summary = "Get product by ID", description = "Returns a single product. 404 if not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product ID"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    // ---------------------------------------------------------
    // GET PAGINATED + SORTED + FILTERED
    // ---------------------------------------------------------
    @Operation(
            summary = "Get filtered, paginated and sorted products",
            description = """
                Supports:
                - Pagination: ?page=0&size=10
                - Sorting: ?sort=price,asc
                - Filtering:
                    ?categoryId=3
                    ?minPrice=100&maxPrice=500
                    ?q=laptop
                """
    )
    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String q,
            Pageable pageable
    ) {
        return productService.searchProducts(categoryId, minPrice, maxPrice, q, pageable);
    }

    // ---------------------------------------------------------
    // CREATE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product", description = "Creates a product and assigns it to a category.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.createProduct(request);
    }

    // ---------------------------------------------------------
    // UPDATE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing product", description = "Updates product fields and category.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product data"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return productService.updateProduct(id, request);
    }

    // ---------------------------------------------------------
    // DELETE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a product", description = "Deletes a product by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }
}