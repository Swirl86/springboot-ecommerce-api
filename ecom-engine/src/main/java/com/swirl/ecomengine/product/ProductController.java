package com.swirl.ecomengine.product;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/seed")
    public List<Product> seedProducts() {
        productService.saveProduct(new Product(null, "Laptop", 999.99, "Powerful laptop"));
        productService.saveProduct(new Product(null, "Headphones", 199.99, "Noise cancelling headphones"));
        productService.saveProduct(new Product(null, "Keyboard", 49.99, "Mechanical keyboard"));
        return productService.getAllProducts();
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }
}

