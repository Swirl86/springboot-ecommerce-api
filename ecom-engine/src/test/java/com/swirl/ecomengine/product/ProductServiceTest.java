package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    @Test
    void getProductById_returnsProductResponse() {
        Category category = new Category(10L, "Electronics");
        Product product = new Product(1L, "Laptop", 999.99, "Powerful laptop", category);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.categoryId()).isEqualTo(10L);
        assertThat(response.categoryName()).isEqualTo("Electronics");
    }

    @Test
    void getProductById_throwsException_whenNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void createProduct_savesAndReturnsResponse() {
        Category category = new Category(10L, "Electronics");
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop", 10L);

        when(categoryService.getById(10L)).thenReturn(category);

        Product saved = new Product(1L, "Laptop", 999.99, "Powerful laptop", category);
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.createProduct(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.categoryId()).isEqualTo(10L);

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_throwsException_whenNotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ProductNotFoundException.class);
    }
}