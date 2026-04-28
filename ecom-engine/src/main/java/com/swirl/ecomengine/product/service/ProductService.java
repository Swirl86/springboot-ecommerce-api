package com.swirl.ecomengine.product.service;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductMapper;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.product.ProductSpecifications;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.product.exception.ProductCategoryMismatchException;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final ProductMapper mapper;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          ProductMapper mapper) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.mapper = mapper;
    }

    // ---------------------------------------------------------
    // INTERNAL: Return entity
    // ---------------------------------------------------------
    private Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(mapper::toResponse);
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    public ProductResponse getProductById(Long id) {
        return mapper.toResponse(getById(id));
    }

    // ---------------------------------------------------------
    // SEARCH WITH FILTERS + PAGINATION + SORTING
    // ---------------------------------------------------------
    public Page<ProductResponse> searchProducts(
            Long categoryId,
            Double minPrice,
            Double maxPrice,
            String searchTerm,
            Pageable pageable
    ) {
        Specification<Product> spec = Specification.where(null);

        spec = spec.and(ProductSpecifications.withCategory(categoryId));
        spec = spec.and(ProductSpecifications.withMinPrice(minPrice));
        spec = spec.and(ProductSpecifications.withMaxPrice(maxPrice));
        spec = spec.and(ProductSpecifications.withSearchTerm(searchTerm));

        return productRepository.findAll(spec, pageable)
                .map(mapper::toResponse);
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getById(request.categoryId());

        Product product = mapper.toEntity(request, category);

        Product saved = productRepository.save(product);
        return mapper.toResponse(saved);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = getById(id);

        Category category = categoryService.getById(request.categoryId());
        if (category == null) {
            throw new ProductCategoryMismatchException(request.categoryId());
        }

        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());
        product.setCategory(category);
        product.setImageUrls(
                request.imageUrls() != null ? request.imageUrls() : List.of()
        );

        Product updated = productRepository.save(product);
        return mapper.toResponse(updated);
    }

    // ---------------------------------------------------------
    // DELETE
    // ---------------------------------------------------------
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        productRepository.deleteById(id);
    }

    // ---------------------------------------------------------
    // ETag helpers
    // ---------------------------------------------------------
    public LocalDateTime getLastUpdated() {
        return productRepository.findLastUpdated();
    }

    public LocalDateTime getLastUpdatedFiltered(Long categoryId, Double minPrice, Double maxPrice, String searchTerm) {
        return productRepository.findLastUpdatedFiltered(categoryId, minPrice, maxPrice, searchTerm);
    }
}