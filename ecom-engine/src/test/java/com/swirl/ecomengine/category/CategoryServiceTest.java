package com.swirl.ecomengine.category;

import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.common.exception.BadRequestException;
import com.swirl.ecomengine.common.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static testsupport.TestDataFactory.category;
import static testsupport.TestDataFactory.defaultCategory;

class CategoryServiceTest {

    private CategoryRepository repo;
    private CategoryMapper mapper;
    private CategoryService service;

    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setup() {
        repo = mock(CategoryRepository.class);
        mapper = mock(CategoryMapper.class);
        service = new CategoryService(repo, mapper);
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------

    @Test
    void getById_shouldReturnCategory_whenExists() {
        Category category = defaultCategory();
        category.setId(1L);

        when(repo.findById(1L)).thenReturn(Optional.of(category));

        Category result = service.getById(1L);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getById(99L))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ---------------------------------------------------------
    // GET BY NAME
    // ---------------------------------------------------------

    @Test
    void findByNameOrThrow_shouldReturnCategory_whenExists() {
        Category category = defaultCategory();

        when(repo.findByNameIgnoreCase("Electronics"))
                .thenReturn(Optional.of(category));

        Category result = service.findByNameOrThrow("Electronics");

        assertThat(result).isEqualTo(category);
    }

    @Test
    void findByNameOrThrow_shouldThrow_whenNotFound() {
        when(repo.findByNameIgnoreCase("Missing"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByNameOrThrow("Missing"))
                .isInstanceOf(CategoryNotFoundException.class)
                .hasMessageContaining("Missing");
    }

    // ---------------------------------------------------------
    // GET CATEGORY BY ID (DTO)
    // ---------------------------------------------------------

    @Test
    void getCategoryById_shouldReturnMappedResponse() {
        Category category = defaultCategory();
        category.setId(1L);

        CategoryResponse response = new CategoryResponse(1L, "Electronics", now);

        when(repo.findById(1L)).thenReturn(Optional.of(category));
        when(mapper.toResponse(category)).thenReturn(response);

        CategoryResponse result = service.getCategoryById(1L);

        assertThat(result).isEqualTo(response);
    }

    // ---------------------------------------------------------
    // GET ALL (PAGINATED)
    // ---------------------------------------------------------

    @Test
    void getAll_shouldReturnPaginatedResponse() {
        Category c1 = category("Electronics");
        c1.setId(1L);

        Category c2 = category("Books");
        c2.setId(2L);

        Pageable pageable = PageRequest.of(0, 20);
        Page<Category> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(repo.findAll(pageable)).thenReturn(page);
        when(mapper.toResponse(c1)).thenReturn(new CategoryResponse(1L, "Electronics", now));
        when(mapper.toResponse(c2)).thenReturn(new CategoryResponse(2L, "Books", now));

        Page<CategoryResponse> result = service.getAll(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).name()).isEqualTo("Electronics");
        assertThat(result.getContent().get(1).name()).isEqualTo("Books");

        verify(repo).findAll(pageable);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoCategoriesExist() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Category> emptyPage = Page.empty(pageable);

        when(repo.findAll(pageable)).thenReturn(emptyPage);

        Page<CategoryResponse> result = service.getAll(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(repo).findAll(pageable);
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnResponse_whenValid() {
        CategoryRequest req = new CategoryRequest("NewCat");

        Category saved = category("NewCat");
        saved.setId(1L);

        CategoryResponse response = new CategoryResponse(1L, "NewCat", now);

        when(repo.findByNameIgnoreCase("NewCat")).thenReturn(Optional.empty());
        when(repo.save(any(Category.class))).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(response);

        CategoryResponse result = service.create(req);

        assertThat(result).isEqualTo(response);

        verify(repo).save(argThat(c -> c.getName().equals("NewCat")));
    }

    @Test
    void create_shouldThrow_whenNameIsBlank() {
        CategoryRequest req = new CategoryRequest("  ");

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    void create_shouldThrow_whenNameAlreadyExists() {
        CategoryRequest req = new CategoryRequest("Electronics");

        when(repo.findByNameIgnoreCase("Electronics"))
                .thenReturn(Optional.of(defaultCategory()));

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------

    @Test
    void update_shouldModifyAndReturnResponse_whenValid() {
        Category existing = defaultCategory();
        existing.setId(1L);

        CategoryRequest req = new CategoryRequest("UpdatedName");

        Category updated = category("UpdatedName");
        updated.setId(1L);

        CategoryResponse response = new CategoryResponse(1L, "UpdatedName", now);

        when(repo.findById(1L)).thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(updated);
        when(mapper.toResponse(updated)).thenReturn(response);

        CategoryResponse result = service.update(1L, req);

        assertThat(result).isEqualTo(response);
        assertThat(existing.getName()).isEqualTo("UpdatedName");
    }

    @Test
    void update_shouldThrow_whenNameIsBlank() {
        CategoryRequest req = new CategoryRequest("");

        assertThatThrownBy(() -> service.update(1L, req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void update_shouldThrow_whenCategoryNotFound() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        CategoryRequest req = new CategoryRequest("NewName");

        assertThatThrownBy(() -> service.update(99L, req))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------

    @Test
    void delete_shouldRemoveCategory_whenExists() {
        when(repo.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repo).deleteById(1L);
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(repo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}