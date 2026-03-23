package com.swirl.ecomengine.category;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    public CategoryService(CategoryRepository repo) {
        this.repo = repo;
    }

    public List<CategoryResponse> getAll() {
        return repo.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        Category category = new Category(null, request.name());
        Category saved = repo.save(category);
        return new CategoryResponse(saved.getId(), saved.getName());
    }

    public Category getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
    }
}
