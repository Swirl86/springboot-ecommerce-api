package com.swirl.ecomengine.config;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRequest;
import com.swirl.ecomengine.category.CategoryService;
import com.swirl.ecomengine.product.ProductRequest;
import com.swirl.ecomengine.product.ProductService;
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

        var electronicsRes = categoryService.create(new CategoryRequest("Electronics"));
        var accessoriesRes = categoryService.create(new CategoryRequest("Accessories"));

        var electronics = categoryService.getById(electronicsRes.id());
        var accessories = categoryService.getById(accessoriesRes.id());

        productService.createProduct(new ProductRequest(
                "Laptop",
                999.99,
                "Powerful laptop",
                electronics.getId()
        ));

        productService.createProduct(new ProductRequest(
                "Headphones",
                199.99,
                "Noise cancelling headphones",
                accessories.getId()
        ));

        productService.createProduct(new ProductRequest(
                "Keyboard",
                49.99,
                "Mechanical keyboard",
                accessories.getId()
        ));
    }
}