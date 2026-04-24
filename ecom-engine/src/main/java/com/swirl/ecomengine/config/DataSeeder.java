package com.swirl.ecomengine.config;

import com.swirl.ecomengine.auth.AuthService;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.service.ProductService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev") // Only runs when running with --spring.profiles.active=dev
public class DataSeeder implements CommandLineRunner {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final UserService userService;
    private final CartService cartService;

    public DataSeeder(
            ProductService productService,
            CategoryService categoryService,
            AuthService authService,
            UserService userService,
            CartService cartService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.authService = authService;
        this.userService =userService;
        this.cartService = cartService;
    }

    @Override
    public void run(String... args) {

        var auth = authService.register(new RegisterRequest(
                "test@gmail.com",
                "12345678"
        ));

        User user = userService.getById(auth.userId());

        var electronics = ensureCategory("Electronics");
        var accessories = ensureCategory("Accessories");
        var home = ensureCategory("Home & Kitchen");
        var clothing = ensureCategory("Clothing");
        var sports = ensureCategory("Sports & Outdoors");

        // -----------------------------
        // Electronics (2–3 images)
        // -----------------------------
        var laptopId = createProduct("Laptop", 999.99, "Powerful laptop", electronics, 3);
        var smartphoneId = createProduct("Smartphone", 699.99, "Latest model smartphone", electronics, 2);
        var tabletId = createProduct("Tablet", 399.99, "Portable tablet", electronics, 2);

        createProduct("Bluetooth Speaker", 89.99, "Wireless speaker", electronics, 1);
        createProduct("Smartwatch", 199.99, "Fitness tracking smartwatch", electronics, 2);

        // -----------------------------
        // Accessories (1 image)
        // -----------------------------
        createProduct("Headphones", 199.99, "Noise cancelling headphones", accessories, 1);
        createProduct("Keyboard", 49.99, "Mechanical keyboard", accessories, 1);
        createProduct("Mouse", 29.99, "Wireless mouse", accessories, 1);
        createProduct("USB-C Cable", 9.99, "Durable USB-C charging cable", accessories, 0);
        createProduct("Laptop Stand", 39.99, "Ergonomic laptop stand", accessories, 1);

        // -----------------------------
        // Home & Kitchen (no images)
        // -----------------------------
        createProduct("Coffee Maker", 79.99, "Automatic coffee machine", home, 0);
        createProduct("Blender", 59.99, "High-speed blender", home, 0);
        createProduct("Air Fryer", 129.99, "Healthy cooking air fryer", home, 0);

        // -----------------------------
        // Clothing (1 image)
        // -----------------------------
        createProduct("T-Shirt", 19.99, "Cotton t-shirt", clothing, 1);
        createProduct("Hoodie", 39.99, "Warm hoodie", clothing, 1);
        createProduct("Jeans", 49.99, "Slim fit jeans", clothing, 1);

        // -----------------------------
        // Sports & Outdoors (0–1 images)
        // -----------------------------
        createProduct("Yoga Mat", 24.99, "Non-slip yoga mat", sports, 0);
        createProduct("Dumbbells", 49.99, "Set of dumbbells", sports, 1);
        createProduct("Running Shoes", 89.99, "Lightweight running shoes", sports, 1);

        // -----------------------------
        // Seed cart
        // -----------------------------
        cartService.addItem(user, laptopId, 3);
        cartService.addItem(user, smartphoneId, 2);
        cartService.addItem(user, tabletId, 1);
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

    private Long createProduct(String name, double price, String desc, Category category, int imageCount) {
        var created = productService.createProduct(new ProductRequest(
                name,
                price,
                desc,
                category.getId(),
                randomImages(imageCount)
        ));

        return created.id();
    }

    private List<String> randomImages(int count) {
        if (count <= 0) return List.of();

        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> "https://picsum.photos/400?random=" + (int)(Math.random() * 10000))
                .toList();
    }
}