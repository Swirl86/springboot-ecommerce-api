package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    // ---------------------------------------------------------
    // MAPPER: Entity -> Response DTO
    // ---------------------------------------------------------
    private ProductResponse toResponse(Product p) {
        return new ProductResponse(
                p.getId(),
                p.getName(),
                p.getPrice(),
                p.getDescription(),
                p.getCategory().getId(),
                p.getCategory().getName()
        );
    }

    // ---------------------------------------------------------
    // GET ALL
    // ---------------------------------------------------------
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ---------------------------------------------------------
    // GET BY ID
    // ---------------------------------------------------------
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return toResponse(product);
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
                .map(this::toResponse);
    }

    // ---------------------------------------------------------
    // CREATE
    // ---------------------------------------------------------
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryService.getById(request.categoryId());

        Product product = new Product(
                null,
                request.name(),
                request.price(),
                request.description(),
                category
        );

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    // ---------------------------------------------------------
    // UPDATE
    // ---------------------------------------------------------
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        product.setName(request.name());
        product.setPrice(request.price());
        product.setDescription(request.description());

        Category category = categoryService.getById(request.categoryId());
        product.setCategory(category);

        Product updated = productRepository.save(product);
        return toResponse(updated);
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
}