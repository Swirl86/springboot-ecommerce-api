package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import testsupport.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryService categoryService;
    private ProductService productService;

    private Category category;
    private Product laptop;
    private Product phone;
    private ProductRequest request;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryService = mock(CategoryService.class);

        ProductMapper productMapper = new ProductMapper();
        productService = new ProductService(productRepository, categoryService, productMapper);

        category = TestDataFactory.defaultCategory();

        laptop = TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category);
        laptop.setId(1L);

        phone = TestDataFactory.product("Phone", 499.99, "Smartphone", category);
        phone.setId(2L);

        request = new ProductRequest("Laptop", 999.99, "Powerful laptop", category.getId());
    }

    // ------------------------------------------------------------
    // getProductById — returns mapped ProductResponse
    // ------------------------------------------------------------
    @Test
    void getProductById_returnsProductResponse() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo(laptop.getName());
        assertThat(response.categoryId()).isEqualTo(category.getId());
        assertThat(response.categoryName()).isEqualTo(category.getName());
    }

    // ------------------------------------------------------------
    // getAllProducts — returns paginated ProductResponse list
    // ------------------------------------------------------------
    @Test
    void getAllProducts_returnsPaginatedResponse() {

        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(laptop, phone), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(page);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        ProductResponse r1 = result.getContent().get(0);
        assertThat(r1.id()).isEqualTo(1L);
        assertThat(r1.name()).isEqualTo(laptop.getName());

        verify(productRepository).findAll(pageable);
    }

    // ------------------------------------------------------------
    // getAllProducts — returns empty page
    // ------------------------------------------------------------
    @Test
    void getAllProducts_returnsEmptyPage_whenNoProductsExist() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> emptyPage = Page.empty(pageable);

        when(productRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(productRepository).findAll(pageable);
    }

    // ------------------------------------------------------------
    // getProductById — throws when product not found
    // ------------------------------------------------------------
    @Test
    void getProductById_throwsException_whenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ------------------------------------------------------------
    // createProduct — saves and returns mapped response
    // ------------------------------------------------------------
    @Test
    void createProduct_savesAndReturnsResponse() {
        when(categoryService.getById(10L)).thenReturn(category);

        when(productRepository.save(any(Product.class))).thenReturn(laptop);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.categoryId()).isEqualTo(category.getId());

        verify(productRepository).save(any(Product.class));
    }

    // ------------------------------------------------------------
    // createProduct — throws when category does not exist
    // ------------------------------------------------------------
    @Test
    void createProduct_throwsException_whenCategoryNotFound() {
        when(categoryService.getById(category.getId()))
                .thenThrow(new CategoryNotFoundException(10L));

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(CategoryNotFoundException.class);
    }

    // ------------------------------------------------------------
    // deleteProduct — throws when product does not exist
    // ------------------------------------------------------------
    @Test
    void deleteProduct_throwsException_whenNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    // ------------------------------------------------------------
    // deleteProduct — deletes when product exists
    // ------------------------------------------------------------
    @Test
    void deleteProduct_deletesWhenExists() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }
}