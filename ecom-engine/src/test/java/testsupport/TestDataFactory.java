package testsupport;

import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Factory class for creating reusable test data objects.
 * Keeps integration tests clean and focused on behavior.
 */
public class TestDataFactory {

    // ============================================================
    // USERS
    // ============================================================

    public static User admin(PasswordEncoder encoder) {
        return new User(
                null,
                "admin@example.com",
                encoder.encode("password"),
                Role.ADMIN
        );
    }

    public static User user(PasswordEncoder encoder) {
        return new User(
                null,
                "user@example.com",
                encoder.encode("password"),
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
}
