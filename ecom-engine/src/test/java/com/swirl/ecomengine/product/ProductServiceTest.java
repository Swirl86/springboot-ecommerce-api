package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.category.exception.CategoryNotFoundException;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testsupport.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    private ProductRepository productRepository;
    private CategoryService categoryService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        categoryService = mock(CategoryService.class);

        ProductMapper productMapper = new ProductMapper();
        productService = new ProductService(productRepository, categoryService, productMapper);
    }

    // ------------------------------------------------------------
    // getProductById — returns mapped ProductResponse
    // ------------------------------------------------------------
    @Test
    void getProductById_returnsProductResponse() {
        Category category = new Category(10L, "Electronics");

        Product product = TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category);
        product.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.categoryId()).isEqualTo(10L);
        assertThat(response.categoryName()).isEqualTo("Electronics");
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
        Category category = new Category(10L, "Electronics");
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", 10L);

        when(categoryService.getById(10L)).thenReturn(category);

        Product saved = TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category);
        saved.setId(1L);

        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.categoryId()).isEqualTo(10L);

        verify(productRepository).save(any(Product.class));
    }

    // ------------------------------------------------------------
    // createProduct — throws when category does not exist
    // ------------------------------------------------------------
    @Test
    void createProduct_throwsException_whenCategoryNotFound() {
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", 10L);

        when(categoryService.getById(10L))
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