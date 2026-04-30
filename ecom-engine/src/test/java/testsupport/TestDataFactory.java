package testsupport;

import com.swirl.ecomengine.address.Address;
import com.swirl.ecomengine.address.dto.AddressResponse;
import com.swirl.ecomengine.address.dto.CreateOrUpdateAddressRequest;
import com.swirl.ecomengine.auth.RefreshToken;
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
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Factory class for creating consistent and reusable test data objects.
 * Keeps tests clean and focused on behavior.
 */
public class TestDataFactory {

    // ============================================================
    // AUTH
    // ============================================================

    public static RegisterRequest registerRequest() {
        return new RegisterRequest("test@example.com", "password123");
    }

    public static LoginRequest loginRequest() {
        return new LoginRequest("test@example.com", "password123");
    }

    public static AuthResponse authResponse() {
        return new AuthResponse(
                1L,
                "test@example.com",
                "USER",
                "access-token",
                "refresh-token"
        );
    }

    // ============================================================
    // REFRESH TOKEN
    // ============================================================

    public static RefreshToken refreshToken(User user, String tokenValue, LocalDateTime expiresAt, boolean revoked) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(tokenValue);
        rt.setUser(user);
        rt.setExpiresAt(expiresAt);
        rt.setRevoked(revoked);
        return rt;
    }

    // ============================================================
    // USERS
    // ============================================================

    /**
     * Creates an ADMIN user using a real PasswordEncoder.
     * Used in integration tests.
     */
    public static User admin(PasswordEncoder encoder) {
        return User.builder()
                .email("admin@example.com")
                .password(encoder.encode("password123"))
                .name("Admin User")
                .phone("0700000000")
                .role(Role.ADMIN)
                .build();
    }

    public static User admin(PasswordEncoder encoder, String email) {
        return User.builder()
                .email(email)
                .password(encoder.encode("password123"))
                .name("Admin User")
                .phone("0700000000")
                .role(Role.ADMIN)
                .build();
    }

    /**
     * Creates a USER using a real PasswordEncoder.
     * Used in integration tests.
     */
    public static User user(PasswordEncoder encoder) {
        return User.builder()
                .email("user@example.com")
                .password(encoder.encode("password123"))
                .name("Test User")
                .phone("0701111111")
                .role(Role.USER)
                .build();
    }

    public static User user(PasswordEncoder encoder, String email) {
        return User.builder()
                .email(email)
                .password(encoder.encode("password123"))
                .name("Test User")
                .phone("0701111111")
                .role(Role.USER)
                .build();
    }

    public static User userNoPassword(String email) {
        return User.builder()
                .email(email)
                .password("hashed") // irrelevant for repository tests
                .name("Test User")
                .phone("0701111111")
                .role(Role.USER)
                .build();
    }

    /**
     * Creates an ADMIN using a fake encoder.
     * Useful for service tests without Spring context.
     */
    public static User admin(Function<String, String> encoder) {
        return User.builder()
                .email("admin@example.com")
                .password(encoder.apply("password123"))
                .name("Admin User")
                .phone("0700000000")
                .role(Role.ADMIN)
                .build();
    }

    /**
     * Creates a USER using a fake encoder (lambda).
     * Used in unit tests (e.g., mapper tests) to avoid Spring context.
     */
    public static User user(Function<String, String> encoder) {
        return User.builder()
                .email("user@example.com")
                .password(encoder.apply("password123"))
                .name("Test User")
                .phone("0701111111")
                .role(Role.USER)
                .build();
    }

    public static User user(Function<String, String> encoder, String email) {
        return User.builder()
                .email(email)
                .password(encoder.apply("password123"))
                .name("Test User")
                .phone("0701111111")
                .role(Role.USER)
                .build();
    }

    // ============================================================
    // ADDRESS
    // ============================================================

    public static Address address(User user) {
        return Address.builder()
                .street("Main Street 1")
                .postalCode("12345")
                .city("Sundsvall")
                .country("Sweden")
                .user(user)
                .build();
    }

    public static CreateOrUpdateAddressRequest addressRequest() {
        return new CreateOrUpdateAddressRequest(
                "Main Street 1",
                "12345",
                "Sundsvall",
                "Sweden"
        );
    }

    public static AddressResponse addressResponse() {
        return new AddressResponse(
                1L,
                "Main Street 1",
                "12345",
                "Sundsvall",
                "Sweden"
        );
    }

    public static CreateOrUpdateAddressRequest updatedAddressRequest() {
        return new CreateOrUpdateAddressRequest(
                "Updated Street",
                "99999",
                "Stockholm",
                "Sweden"
        );
    }

    public static AddressResponse updatedAddressResponse() {
        return new AddressResponse(
                1L,
                "Updated Street",
                "99999",
                "Stockholm",
                "Sweden"
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
        Product p = new Product();
        p.setId(null);
        p.setName(name);
        p.setPrice(price);
        p.setDescription(desc);
        p.setCategory(category);
        p.setImageUrls(List.of("https://example.com/test-image.jpg"));
        return p;
    }

    public static Product defaultProduct(Category category) {
        Product p = new Product();
        p.setId(null);
        p.setName("Laptop");
        p.setPrice(999.99);
        p.setDescription("Powerful laptop");
        p.setCategory(category);
        p.setImageUrls(List.of("https://example.com/laptop.jpg"));
        return p;
    }

    public static ProductRequest productRequest(Long categoryId) {
        return new ProductRequest(
                "Laptop",
                999.99,
                "Powerful laptop",
                categoryId,
                List.of("https://example.com/laptop.jpg")
        );
    }

    public static ProductRequest updateProductRequest(Long categoryId) {
        return new ProductRequest(
                "Updated",
                123.45,
                "New desc",
                categoryId,
                List.of("img1.jpg", "img2.jpg")
        );
    }

    public static ProductResponse productResponse(Long id, Long categoryId, String categoryName) {
        return new ProductResponse(
                id,
                "Laptop",
                999.99,
                "Powerful laptop",
                categoryId,
                categoryName,
                List.of("https://example.com/laptop.jpg")
        );
    }

    public static ProductResponse defaultProductResponse(Long id) {
        return productResponse(id, 10L, "Electronics");
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
                .deleted(false)
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