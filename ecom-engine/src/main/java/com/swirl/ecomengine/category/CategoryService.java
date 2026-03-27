package com.swirl.ecomengine.category;

import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public Optional<Category> findByName(String name) {
        return repo.findByNameIgnoreCase(name);
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
    public List<CategoryResponse> getAll() {
        return repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public CategoryResponse create(CategoryRequest request) {
        Category saved = repo.save(new Category(null, request.name()));
        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getById(id);
        category.setName(request.name());
        Category updated = repo.save(category);
        return new CategoryResponse(updated.getId(), updated.getName());
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
}
