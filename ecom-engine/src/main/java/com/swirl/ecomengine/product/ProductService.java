package com.swirl.ecomengine.product;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();

    public ProductService() {
        // TODO : Remove Dummy data used for early testing
        products.add(new Product(1L, "Laptop", 999.99, "Powerful laptop"));
        products.add(new Product(2L, "Headphones", 199.99, "Noise cancelling headphones"));
        products.add(new Product(3L, "Keyboard", 49.99, "Mechanical keyboard"));
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProductById(Long id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}