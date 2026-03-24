package com.swirl.ecomengine.category;

import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
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
    public CategoryResponse getByIdResponse(Long id) {
        Category category = getById(id);
        return new CategoryResponse(category.getId(), category.getName());
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    public List<CategoryResponse> getAll() {
        return repo.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category(null, request.name());
        Category saved = repo.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
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
