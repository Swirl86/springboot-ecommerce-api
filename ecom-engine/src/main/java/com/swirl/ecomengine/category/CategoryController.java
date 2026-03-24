package com.swirl.ecomengine.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Categories", description = "Operations for managing product categories")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @Operation(summary = "Get all categories", description = "Returns all categories.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    })
    @GetMapping
    public List<CategoryResponse> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Create a new category", description = "Creates a category. Name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid category data")
    })
    @PostMapping
    public CategoryResponse create(@RequestBody CategoryRequest request) {
        return service.create(request);
    }
}