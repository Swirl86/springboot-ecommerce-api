package testsupport;

import com.swirl.ecomengine.auth.dto.AuthResponse;
import com.swirl.ecomengine.auth.dto.LoginRequest;
import com.swirl.ecomengine.auth.dto.RegisterRequest;
import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.order.Order;
import com.swirl.ecomengine.order.OrderStatus;
import com.swirl.ecomengine.order.item.OrderItem;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Factory class for creating consistent and reusable test data objects.
 * Keeps tests clean and focused on behavior.
 */
public class TestDataFactory {

    // ============================================================
    // AUTH
    // ============================================================

    /**
     * Creates a default RegisterRequest for auth tests.
     */
    public static RegisterRequest registerRequest() {
        return new RegisterRequest("test@example.com", "password123");
    }

    /**
     * Creates a default LoginRequest for auth tests.
     */
    public static LoginRequest loginRequest() {
        return new LoginRequest("test@example.com", "password123");
    }

    /**
     * Creates a default AuthResponse used in controller/service tests.
     */
    public static AuthResponse authResponse() {
        return new AuthResponse(
                1L,
                "test@example.com",
                "USER",
                "jwt-token"
        );
    }

    // ============================================================
    // USERS
    // ============================================================

    /**
     * Creates an ADMIN user using a real PasswordEncoder.
     * Used in integration tests.
     */
    public static User admin(PasswordEncoder encoder) {
        return new User(
                null,
                "admin@example.com",
                encoder.encode("password123"),
                Role.ADMIN
        );
    }

    public static User admin(PasswordEncoder encoder, String email) {
        return new User(
                null,
                email,
                encoder.encode("password123"),
                Role.ADMIN
        );
    }

    /**
     * Creates a USER using a real PasswordEncoder.
     * Used in integration tests.
     */
    public static User user(PasswordEncoder encoder) {
        return new User(
                null,
                "user@example.com",
                encoder.encode("password123"),
                Role.USER
        );
    }

    public static User user(PasswordEncoder encoder, String email) {
        return new User(
                null,
                email,
                encoder.encode("password123"),
                Role.USER
        );
    }

    /**
     * Creates an ADMIN using a fake encoder.
     * Useful for service tests without Spring context.
     */
    public static User admin(Function<String, String> encoder) {
        return new User(
                null,
                "admin@example.com",
                encoder.apply("password123"),
                Role.ADMIN
        );
    }

    /**
     * Creates a USER using a fake encoder (lambda).
     * Used in unit tests (e.g., mapper tests) to avoid Spring context.
     */
    public static User user(Function<String, String> encoder) {
        return new User(
                null,
                "user@example.com",
                encoder.apply("password123"),
                Role.USER
        );
    }

    // ============================================================
    // CATEGORY
    // ============================================================

    public static Category category(String name) {
        return new Category(null, name);
    }

    public static Category defaultCategory() {
        return new Category(null, "Electronics");
    }

    // ============================================================
    // PRODUCT
    // ============================================================

    public static Product product(String name, double price, String desc, Category category) {
        return new Product(
                null,
                name,
                price,
                desc,
                category
        );
    }

    public static Product defaultProduct(Category category) {
        return new Product(
                null,
                "Laptop",
                999.99,
                "Powerful laptop",
                category
        );
    }

    // ============================================================
    // CART
    // ============================================================

    public static Cart cart(User user) {
        return new Cart(user);
    }

    public static CartItem cartItem(Product product, int quantity) {
        return new CartItem(
                product,
                quantity,
                product.getPrice()
        );
    }

    // ============================================================
    // ORDER
    // ============================================================

    public static Order order(User user) {
        return Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static OrderItem orderItem(Order order, Product product, int quantity) {
        return OrderItem.builder()
                .order(order)
                .productId(product.getId())
                .productName(product.getName())
                .price(product.getPrice())
                .quantity(quantity)
                .build();
    }
}