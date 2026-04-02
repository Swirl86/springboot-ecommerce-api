package com.swirl.ecomengine.category.controller;

import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Operations for managing product categories")
@RestController
@RequestMapping("/categories")
@Validated
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    @Operation(summary = "Get all categories", description = "Returns all categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public List<CategoryResponse> getAll() {
        return service.getAll();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    @Operation(summary = "Get category by ID", description = "Returns a single category. 404 if not found.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category ID"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public CategoryResponse getCategory(@PathVariable @Positive(message = "Category ID must be positive") Long id) {
        return service.getCategoryById(id);
    }

    // ---------------------------------------------------------
    // CREATE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new category", description = "Creates a category. Name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return service.create(request);
    }

    // ---------------------------------------------------------
    // UPDATE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing category", description = "Updates category name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    public CategoryResponse update(
            @PathVariable @Positive(message = "Category ID must be positive") Long id,
            @Valid @RequestBody CategoryRequest request
    ) {
        return service.update(id, request);
    }

    // ---------------------------------------------------------
    // DELETE (ADMIN ONLY)
    // ---------------------------------------------------------
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a category", description = "Deletes a category by ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive(message = "Category ID must be positive") Long id) {
        service.delete(id);
    }
}