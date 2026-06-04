package com.swirl.ecomengine.config;

import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.address.service.AddressService;
import com.swirl.ecomengine.auth.AuthService;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.cart.service.CartService;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.service.CategoryService;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.service.OrderService;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.service.ProductService;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.dto.UpdateUserProfileRequest;
import com.swirl.ecomengine.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final AuthService authService;
    private final UserService userService;
    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;

    public DataSeeder(
            ProductService productService,
            CategoryService categoryService,
            AuthService authService,
            UserService userService,
            AddressService addressService,
            CartService cartService,
            OrderService orderService
    ) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.authService = authService;
        this.userService = userService;
        this.addressService = addressService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @Override
    public void run(String... args) {

       if (productService.hasExistingProducts()) return;

        // ---------------------------------------------------------
        // USERS
        // ---------------------------------------------------------
        User simpleUser = createSimpleUser();
        User fullUser = createFullUser();
        User cleanUser = createCleanUser();
        User adminUser = createAdminUser();

        // ---------------------------------------------------------
        // CATEGORIES
        // ---------------------------------------------------------
        Categories categories = createCategories();

        // ---------------------------------------------------------
        // PRODUCTS
        // ---------------------------------------------------------
        ProductIds productIds = createProducts(categories);

        // ---------------------------------------------------------
        // CART
        // ---------------------------------------------------------
        seedCart(simpleUser, productIds);
        seedOrderHistoryForFullUser(fullUser, adminUser, productIds);

        System.out.println("➡️ Dev data seeded successfully.");
    }

    // ---------------------------------------------------------
    // USERS
    // ---------------------------------------------------------
    private User createSimpleUser() {
        var auth = authService.register(new RegisterRequest(
                "test@test.com",
                "12345678"
        ));
        return userService.getById(auth.userId());
    }

    private User createCleanUser() {
        var authClean = authService.register(new RegisterRequest(
                "clean@test.com",
                "12345678"
        ));
        return userService.getById(authClean.userId());
    }

    private User createFullUser() {
        // 1. Register
        var authFull = authService.register(new RegisterRequest(
                "full@test.com",
                "12345678"
        ));

        // 2. Load
        User fullUser = userService.getById(authFull.userId());

        // 3. Update profile
        userService.updateProfile(
                fullUser,
                new UpdateUserProfileRequest(
                        "Anna Andersson",
                        "full@test.com",
                        "0701234567",
                        null,
                        null
                )
        );

        // 4. Update address
        addressService.createOrUpdate(
                fullUser,
                new CreateOrUpdateAddressRequest(
                        "Storgatan 12",
                        "65432",
                        "Karlstad",
                        "Sweden"
                )
        );

        fullUser = userService.getById(fullUser.getId());

        return fullUser;
    }

    private User createAdminUser() {
        // 1. Register admin as a normal user
        var auth = authService.register(new RegisterRequest(
                "admin@test.com",
                "12345678"
        ));

        // 2. Load the user
        User admin = userService.getById(auth.userId());

        // 3. Promote to ADMIN
        admin.setRole(Role.ADMIN);
        admin.setName("Admin User");
        admin.setPhone("0700000000");

        // 4. Save updated user
        return userService.update(admin);
    }

    // ---------------------------------------------------------
    // CATEGORIES
    // ---------------------------------------------------------
    private Categories createCategories() {
        return new Categories(
                ensureCategory("Electronics"),
                ensureCategory("Accessories"),
                ensureCategory("Home & Kitchen"),
                ensureCategory("Clothing"),
                ensureCategory("Sports & Outdoors"),
                ensureCategory("Edge Tests"),
                ensureCategory("Empty Category")
        );
    }

    private Category ensureCategory(String name) {
        return categoryService.findByName(name)
                .orElseGet(() -> {
                    var created = categoryService.create(new CategoryRequest(name));
                    return categoryService.getById(created.id());
                });
    }

    // ---------------------------------------------------------
    // PRODUCTS
    // ---------------------------------------------------------
    private ProductIds createProducts(Categories c) {
        // ---------------------------------------------------------
        // Electronics
        // ---------------------------------------------------------
        Long laptopId = createProduct(
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
                c.electronics(),
                3
        );

        Long smartphoneId = createProduct(
                "Smartphone",
                699.99,
                """
                Latest model smartphone 📱
                Featuring a 6.7" AMOLED display, 120Hz refresh rate,
                48MP triple‑camera system, and 5G connectivity.
                """,
                c.electronics(),
                2
        );

        Long tabletId = createProduct(
                "Tablet",
                399.99,
                """
                Lightweight tablet ideal for reading, streaming, and travel.
                Battery life up to 12 hours. Supports stylus input.
                """,
                c.electronics(),
                2
        );

        createProduct("Bluetooth Speaker", 89.99,
                "Wireless speaker 🔊 with deep bass and 12h battery life.",
                c.electronics(), 0);

        createProduct("Smartwatch", 199.99,
                "Fitness tracking smartwatch ⌚ with heart‑rate monitor and GPS.",
                c.electronics(), 2);

        // ---------------------------------------------------------
        // Accessories
        // ---------------------------------------------------------
        createProduct("Headphones", 199.99,
                "Noise‑cancelling over‑ear headphones 🎧 with deep bass and 40h battery.",
                c.accessories(), 1);

        createProduct("Keyboard", 49.99,
                "Mechanical keyboard with blue switches — clicky and satisfying.",
                c.accessories(), 3);

        createProduct("Mouse", 29.99,
                "Wireless mouse with ergonomic design.",
                c.accessories(), 1);

        createProduct("USB‑C Cable", 9.99,
                "Durable braided USB‑C cable (1.5m). Supports fast charging.",
                c.accessories(), 0);

        createProduct("Laptop Stand", 39.99,
                "Ergonomic laptop stand for better posture.",
                c.accessories(), 2);

        // ---------------------------------------------------------
        // Home & Kitchen
        // ---------------------------------------------------------
        createProduct("Coffee Maker", 79.99,
                """
                Wake up to the smell of fresh coffee ☕
                This automatic coffee maker brews rich, full‑bodied coffee in minutes.
                """,
                c.home(), 0);

        createProduct("Blender", 59.99,
                "High‑speed blender with stainless steel blades.",
                c.home(), 1);

        createProduct("Air Fryer", 129.99,
                """
                Healthy cooking made easy.
                Crispy fries, juicy chicken, and roasted veggies — all with 80% less oil.
                """,
                c.home(), 0);

        // ---------------------------------------------------------
        // Clothing
        // ---------------------------------------------------------
        createProduct("T‑Shirt", 19.99,
                "Soft cotton t‑shirt available in multiple colors.",
                c.clothing(), 1);

        createProduct("Hoodie", 39.99,
                """
                Warm fleece hoodie with kangaroo pocket.
                Perfect for chilly evenings or gym warm‑ups.
                """,
                c.clothing(), 3);

        createProduct("Jeans", 49.99,
                """
                Slim‑fit jeans with stretch denim for comfort.
                Classic 5‑pocket design.
                """,
                c.clothing(), 0);

        // ---------------------------------------------------------
        // Sports & Outdoors
        // ---------------------------------------------------------
        createProduct("Yoga Mat", 24.99,
                "Non‑slip yoga mat 🧘‍♀️ — 6mm thick for extra comfort.",
                c.sports(), 0);

        createProduct("Dumbbells", 49.99,
                "Set of 2×5kg dumbbells — rubber‑coated for durability.",
                c.sports(), 1);

        createProduct("Running Shoes", 89.99,
                """
                Lightweight running shoes with breathable mesh.
                Designed for comfort on long runs. 🏃‍♂️
                """,
                c.sports(), 1);

        // ---------------------------------------------------------
        // Edge‑case test products
        // ---------------------------------------------------------
        createProduct(
                "Ultra Description Test",
                15.00,
                "A".repeat(1500),
                c.edgeTests(),
                2
        );

        createProduct(
                "Special Chars Test",
                5.99,
                "Symbols: !@#$%^&*()_+{}[]|:\";'<>?,./~`",
                c.edgeTests(),
                3
        );

        createProduct(
                "Unicode Test",
                7.99,
                "🔥 Unicode mix test 🚀✨ 🌍 — 日本語 + 한국인 + emojis ✔️ — rendering check. 💬",
                c.edgeTests(),
                1
        );

        return new ProductIds(laptopId, smartphoneId, tabletId);
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

    // ---------------------------------------------------------
    // CART
    // ---------------------------------------------------------
    private void seedCart(User user, ProductIds ids) {
        cartService.addItem(user, ids.laptopId(), 3);
        cartService.addItem(user, ids.smartphoneId(), 2);
        cartService.addItem(user, ids.tabletId(), 1);
    }

    private void seedOrderHistoryForFullUser(User fullUser, User adminUser, ProductIds ids) {

        // ---------------------------
        // 1. COMPLETED ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.laptopId(), 1);
        var completed = orderService.placeOrder(fullUser);
        orderService.updateStatus(completed.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");
        orderService.updateStatus(completed.getId(), OrderStatus.SHIPPED, adminUser, "Auto-seed");
        orderService.updateStatus(completed.getId(), OrderStatus.COMPLETED, adminUser, "Auto-seed");

        // ---------------------------
        // 2. SHIPPED ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.laptopId(), 5);
        var shipped = orderService.placeOrder(fullUser);
        orderService.updateStatus(shipped.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");
        orderService.updateStatus(shipped.getId(), OrderStatus.SHIPPED, adminUser, "Auto-seed");

        // ---------------------------
        // 3. RETURNED ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.smartphoneId(), 3);
        var returned = orderService.placeOrder(fullUser);
        orderService.updateStatus(returned.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");
        orderService.updateStatus(returned.getId(), OrderStatus.SHIPPED, adminUser, "Auto-seed");
        orderService.updateStatus(returned.getId(), OrderStatus.COMPLETED, adminUser, "Auto-seed");
        orderService.updateStatus(returned.getId(), OrderStatus.RETURN_REQUESTED, adminUser, "Auto-seed");
        orderService.updateStatus(returned.getId(), OrderStatus.RETURNED, adminUser, "Auto-seed");

        // ---------------------------
        // 4. REFUNDED ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.tabletId(), 2);
        var refunded = orderService.placeOrder(fullUser);
        orderService.updateStatus(refunded.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");
        orderService.updateStatus(refunded.getId(), OrderStatus.SHIPPED, adminUser, "Auto-seed");
        orderService.updateStatus(refunded.getId(), OrderStatus.COMPLETED, adminUser, "Auto-seed");
        orderService.updateStatus(refunded.getId(), OrderStatus.RETURN_REQUESTED, adminUser, "Auto-seed");
        orderService.updateStatus(refunded.getId(), OrderStatus.RETURNED, adminUser, "Auto-seed");
        orderService.updateStatus(refunded.getId(), OrderStatus.REFUNDED, adminUser, "Auto-seed");

        // ---------------------------
        // 5. CANCELLED ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.laptopId(), 1);
        var cancelled = orderService.placeOrder(fullUser);
        orderService.updateStatus(cancelled.getId(), OrderStatus.CANCELLED, adminUser, "Auto-seed");

        // ---------------------------
        // 6. PENDING ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.smartphoneId(), 8);
        orderService.placeOrder(fullUser); // stays PENDING

        // ---------------------------
        // 7. PROCESSING ORDER
        // ---------------------------
        cartService.addItem(fullUser, ids.laptopId(), 2);
        var processing = orderService.placeOrder(fullUser);
        orderService.updateStatus(processing.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");

        // ---------------------------
        // 8. RETURN_REQUESTED ORDER (ACTIVE)
        // ---------------------------
        cartService.addItem(fullUser, ids.tabletId(), 1);
        var returnRequested = orderService.placeOrder(fullUser);
        orderService.updateStatus(returnRequested.getId(), OrderStatus.PROCESSING, adminUser, "Auto-seed");
        orderService.updateStatus(returnRequested.getId(), OrderStatus.SHIPPED, adminUser, "Auto-seed");
        orderService.updateStatus(returnRequested.getId(), OrderStatus.COMPLETED, adminUser, "Auto-seed");
        orderService.updateStatus(returnRequested.getId(), OrderStatus.RETURN_REQUESTED, adminUser, "Auto-seed");
    }

    // ---------------------------------------------------------
    // Helper records
    // ---------------------------------------------------------
    private record Categories(
            Category electronics,
            Category accessories,
            Category home,
            Category clothing,
            Category sports,
            Category edgeTests,
            Category empty
    ) {}

    private record ProductIds(
            Long laptopId,
            Long smartphoneId,
            Long tabletId
    ) {}
}