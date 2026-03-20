package com.swirl.ecomengine.product;

import com.swirl.ecomengine.product.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(
                        product.getId(),
                        product.getName(),
                        product.getPrice(),
                        product.getDescription()
                ))
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription()
        );
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(
                null,
                request.name(),
                request.price(),
                request.description()
        );

        Product saved = productRepository.save(product);

        return new ProductResponse(
                saved.getId(),
                saved.getName(),
                saved.getPrice(),
                saved.getDescription()
        );
    }

    // Only used by seed
    public void saveProduct(Product product) {
        productRepository.save(product);
    }
}