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
        var edgeTests = ensureCategory("Edge Tests");
        ensureCategory("Empty Category");


        // ---------------------------------------------------------
        // Electronics
        // ---------------------------------------------------------
        var laptopId = createProduct(
                "Laptop",
                999.99,
                """
                Powerful laptop with:
                - Intel i7 13th Gen
                - 16GB DDR5 RAM
                - 1TB NVMe SSD
                - 15.6" 165Hz IPS Display
                Perfect for gaming, development, and productivity.
                """,
                electronics,
                3
        );

        var smartphoneId = createProduct(
                "Smartphone",
                699.99,
                """
                Latest model smartphone 📱
                Featuring a 6.7" AMOLED display, 120Hz refresh rate,
                48MP triple‑camera system, and 5G connectivity.
                """,
                electronics,
                2
        );

        var tabletId = createProduct(
                "Tablet",
                399.99,
                """
                Lightweight tablet ideal for reading, streaming, and travel.
                Battery life up to 12 hours. Supports stylus input.
                """,
                electronics,
                2
        );

        createProduct("Bluetooth Speaker", 89.99,
                "Wireless speaker 🔊 with deep bass and 12h battery life.",
                electronics, 0);

        createProduct("Smartwatch", 199.99,
                "Fitness tracking smartwatch ⌚ with heart‑rate monitor and GPS.",
                electronics, 2);

        // ---------------------------------------------------------
        // Accessories
        // ---------------------------------------------------------
        createProduct("Headphones", 199.99,
                "Noise‑cancelling over‑ear headphones 🎧 with deep bass and 40h battery.",
                accessories, 1);

        createProduct("Keyboard", 49.99,
                "Mechanical keyboard with blue switches — clicky and satisfying.",
                accessories, 3);

        createProduct("Mouse", 29.99,
                "Wireless mouse with ergonomic design.",
                accessories, 1);

        createProduct("USB‑C Cable", 9.99,
                "Durable braided USB‑C cable (1.5m). Supports fast charging.",
                accessories, 0);

        createProduct("Laptop Stand", 39.99,
                "Ergonomic laptop stand for better posture.",
                accessories, 2);

        // ---------------------------------------------------------
        // Home & Kitchen
        // ---------------------------------------------------------
        createProduct("Coffee Maker", 79.99,
                """
                Wake up to the smell of fresh coffee ☕
                This automatic coffee maker brews rich, full‑bodied coffee in minutes.
                """,
                home, 0);

        createProduct("Blender", 59.99,
                "High‑speed blender with stainless steel blades.",
                home, 1);

        createProduct("Air Fryer", 129.99,
                """
                Healthy cooking made easy.
                Crispy fries, juicy chicken, and roasted veggies — all with 80% less oil.
                """,
                home, 0);

        // ---------------------------------------------------------
        // Clothing
        // ---------------------------------------------------------
        createProduct("T‑Shirt", 19.99,
                "Soft cotton t‑shirt available in multiple colors.",
                clothing, 1);

        createProduct("Hoodie", 39.99,
                """
                Warm fleece hoodie with kangaroo pocket.
                Perfect for chilly evenings or gym warm‑ups.
                """,
                clothing, 3);

        createProduct("Jeans", 49.99,
                """
                Slim‑fit jeans with stretch denim for comfort.
                Classic 5‑pocket design.
                """,
                clothing, 0);

        // ---------------------------------------------------------
        // Sports & Outdoors
        // ---------------------------------------------------------
        createProduct("Yoga Mat", 24.99,
                "Non‑slip yoga mat 🧘‍♀️ — 6mm thick for extra comfort.",
                sports, 0);

        createProduct("Dumbbells", 49.99,
                "Set of 2×5kg dumbbells — rubber‑coated for durability.",
                sports, 1);

        createProduct("Running Shoes", 89.99,
                """
                Lightweight running shoes with breathable mesh.
                Designed for comfort on long runs. 🏃‍♂️
                """,
                sports, 1);

        // ---------------------------------------------------------
        // Edge‑case test products
        // ---------------------------------------------------------
        createProduct(
                "Ultra Description Test",
                15.00,
                "A".repeat(1500),
                edgeTests,
                2
        );

        createProduct(
                "Special Chars Test",
                5.99,
                "Symbols: !@#$%^&*()_+{}[]|:\";'<>?,./~`",
                edgeTests,
                3
        );

        createProduct(
                "Unicode Test",
                7.99,
                "🔥 Unicode mix test \uD83D\uDE80✨ \uD83C\uDF0D — 日本語 + 한국인 + emojis ✔\uFE0F — rendering check. 💬",
                edgeTests,
                1
        );

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