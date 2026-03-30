package com.swirl.ecomengine.config;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.product.ProductService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev") // Only runs when running with --spring.profiles.active=dev
public class DataSeeder implements CommandLineRunner {

    private final ProductService productService;
    private final CategoryService categoryService;

    public DataSeeder(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Override
    public void run(String... args) {

        // -----------------------------
        // Create or fetch categories
        // -----------------------------
        var electronics = ensureCategory("Electronics");
        var accessories = ensureCategory("Accessories");
        var home = ensureCategory("Home & Kitchen");
        var clothing = ensureCategory("Clothing");
        var sports = ensureCategory("Sports & Outdoors");

        // -----------------------------
        // Electronics
        // -----------------------------
        createProduct("Laptop", 999.99, "Powerful laptop", electronics);
        createProduct("Smartphone", 699.99, "Latest model smartphone", electronics);
        createProduct("Tablet", 399.99, "Portable tablet", electronics);
        createProduct("Bluetooth Speaker", 89.99, "Wireless speaker", electronics);
        createProduct("Smartwatch", 199.99, "Fitness tracking smartwatch", electronics);

        // -----------------------------
        // Accessories
        // -----------------------------
        createProduct("Headphones", 199.99, "Noise cancelling headphones", accessories);
        createProduct("Keyboard", 49.99, "Mechanical keyboard", accessories);
        createProduct("Mouse", 29.99, "Wireless mouse", accessories);
        createProduct("USB-C Cable", 9.99, "Durable USB-C charging cable", accessories);
        createProduct("Laptop Stand", 39.99, "Ergonomic laptop stand", accessories);

        // -----------------------------
        // Home & Kitchen
        // -----------------------------
        createProduct("Coffee Maker", 79.99, "Automatic coffee machine", home);
        createProduct("Blender", 59.99, "High-speed blender", home);
        createProduct("Air Fryer", 129.99, "Healthy cooking air fryer", home);

        // -----------------------------
        // Clothing
        // -----------------------------
        createProduct("T-Shirt", 19.99, "Cotton t-shirt", clothing);
        createProduct("Hoodie", 39.99, "Warm hoodie", clothing);
        createProduct("Jeans", 49.99, "Slim fit jeans", clothing);

        // -----------------------------
        // Sports & Outdoors
        // -----------------------------
        createProduct("Yoga Mat", 24.99, "Non-slip yoga mat", sports);
        createProduct("Dumbbells", 49.99, "Set of dumbbells", sports);
        createProduct("Running Shoes", 89.99, "Lightweight running shoes", sports);
    }

    // ---------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------
    private Category ensureCategory(String name) {
        return categoryService.findByName(name)
                .orElseGet(() -> {
                    var created = categoryService.create(new CategoryRequest(name));
                    return categoryService.getById(created.id());
                });
    }

    private void createProduct(String name, double price, String desc, Category category) {
        productService.createProduct(new ProductRequest(
                name,
                price,
                desc,
                category.getId()
        ));
    }
}