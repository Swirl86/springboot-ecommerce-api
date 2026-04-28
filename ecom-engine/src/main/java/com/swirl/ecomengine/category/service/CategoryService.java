package com.swirl.ecomengine.category.service;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryMapper;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ConflictException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository repo;
    private final CategoryMapper mapper;

    public CategoryService(CategoryRepository repo, CategoryMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // INTERNAL: Return entity
    // ---------------------------------------------------------
    public Category getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    // ---------------------------------------------------------
    // GET BY NAME
    // ---------------------------------------------------------
    public Optional<Category> findByName(String name) {
        return repo.findByNameIgnoreCase(name);
    }

    public Category findByNameOrThrow(String name) {
        return repo.findByNameIgnoreCase(name)
                .orElseThrow(() -> new CategoryNotFoundException(name));
    }


    // ---------------------------------------------------------
    // API: Return response DTO
    // ---------------------------------------------------------
    public CategoryResponse getCategoryById(Long id) {
        return mapper.toResponse(getById(id));
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    public Page<CategoryResponse> getAll(Pageable pageable) {
        return repo.findAll(pageable)
                .map(mapper::toResponse);
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public CategoryResponse create(CategoryRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        if (repo.findByNameIgnoreCase(request.name()).isPresent()) {
            throw new ConflictException("Category with name '" + request.name() + "' already exists");
        }

        Category saved = repo.save(new Category(null, request.name()));

        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    public CategoryResponse update(Long id, CategoryRequest request) {

        if (request.name() == null || request.name().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        Category category = getById(id);
        category.setName(request.name());

        Category updated = repo.save(category);
        return mapper.toResponse(updated);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        repo.deleteById(id);
    }

    // ---------------------------------------------------------
    // ETag helpers
    // ---------------------------------------------------------
    public LocalDateTime getLastUpdated() {
        return repo.findLastUpdated();
    }
}