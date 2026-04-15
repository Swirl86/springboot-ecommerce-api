package com.swirl.ecomengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swirl.ecomengine.cart.Cart;
import com.swirl.ecomengine.cart.CartRepository;
import com.swirl.ecomengine.cart.item.CartItem;
import com.swirl.ecomengine.category.Category;
import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.product.Product;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import testsupport.IntegrationTestBase;
import testsupport.TestDataFactory;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIntegrationTest extends IntegrationTestBase {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private OrderRepository orderRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @Autowired private JwtService jwtService;
    @Autowired private PasswordEncoder passwordEncoder;

    private String adminToken;
    private User user;
    private String userToken;
    private Product laptop;
    private Product tablet;

    @BeforeEach
    void setup() {
        User admin = userRepository.save(TestDataFactory.admin(passwordEncoder));
        adminToken = jwtService.generateToken(admin);

        user = userRepository.save(TestDataFactory.user(passwordEncoder));
        userToken = jwtService.generateToken(user);

        Category category = categoryRepository.save(TestDataFactory.defaultCategory());

        laptop = productRepository.save(TestDataFactory.product("Laptop", 999.99, "Powerful laptop", category));
        tablet = productRepository.save(TestDataFactory.product("Tablet", 399.99, "Portable tablet", category));

        Cart cart = new Cart(user);

        CartItem item1 = new CartItem(laptop, 2, laptop.getPrice());
        CartItem item2 = new CartItem(tablet, 1, tablet.getPrice());

        item1.setCart(cart);
        item2.setCart(cart);

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        cartRepository.save(cart);
    }

    // ---------------------------------------------------------
    // CHECKOUT
    // ---------------------------------------------------------
    @Test
    void checkout_shouldCreateOrderSuccessfully() throws Exception {
        MvcResult result = mockMvc.perform(post("/orders/checkout")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items").isArray())
                .andReturn();

        var json = objectMapper.readTree(result.getResponse().getContentAsString());

        assertThat(json.get("totalPrice").asDouble())
                .isEqualTo(2 * laptop.getPrice() + tablet.getPrice());

        assertThat(json.get("items").size()).isEqualTo(2);
        assertThat(json.get("status").asText()).isEqualTo(OrderStatus.PENDING.name());
    }

    // ---------------------------------------------------------
    // GET ORDERS (Pagination)
    // ---------------------------------------------------------
    @Test
    void getOrders_shouldReturnPaginatedOrders() throws Exception {
        // Create 3 orders for the user
        Order o1 = orderRepository.save(Order.builder()
                .user(user).totalPrice(10).createdAt(LocalDateTime.now()).build());

        Order o2 = orderRepository.save(Order.builder()
                .user(user).totalPrice(20).createdAt(LocalDateTime.now()).build());

        Order o3 = orderRepository.save(Order.builder()
                .user(user).totalPrice(30).createdAt(LocalDateTime.now()).build());

        // Request page=0, size=2
        mockMvc.perform(get("/orders?page=0&size=2")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(2));
    }

    // ---------------------------------------------------------
    // DELETE /orders/{id} — soft-delete behavior
    // ---------------------------------------------------------
    @Test
    void deleteOrder_shouldSoftDelete() throws Exception {
        Order order = orderRepository.save(TestDataFactory.order(user));

        mockMvc.perform(delete("/orders/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Order should no longer be found
        mockMvc.perform(get("/orders/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());

        // But it remains in the database.
        Order deleted = (Order) entityManager
                .createNativeQuery("SELECT * FROM orders WHERE id = :id", Order.class)
                .setParameter("id", order.getId())
                .getSingleResult();

        assertThat(deleted.isDeleted()).isTrue();
    }

    // ---------------------------------------------------------
    // GET /orders/deleted — list soft-deleted orders (ADMIN)
    // ---------------------------------------------------------
    @Test
    void getDeletedOrders_shouldReturnSoftDeletedOrdersForAdmin() throws Exception {
        // Create two orders
        Order o1 = orderRepository.save(TestDataFactory.order(user));
        Order o2 = orderRepository.save(TestDataFactory.order(user));

        // Soft-delete one of them
        mockMvc.perform(delete("/orders/" + o1.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Admin requests deleted orders
        mockMvc.perform(get("/orders/deleted")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(o1.getId()));
    }

    // ---------------------------------------------------------
    // RESTORE /orders/{id}/restore — restore soft-deleted order
    // ---------------------------------------------------------
    @Test
    void restoreOrder_shouldRestoreSoftDeletedOrder() throws Exception {
        // Create order
        Order order = orderRepository.save(TestDataFactory.order(user));

        // Soft-delete it
        mockMvc.perform(delete("/orders/" + order.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Confirm it is no longer accessible via API
        mockMvc.perform(get("/orders/" + order.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());

        // Restore the order
        mockMvc.perform(post("/orders/" + order.getId() + "/restore")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Confirm it is accessible again (USER, not admin)
        mockMvc.perform(get("/orders/" + order.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));

        // Confirm in DB that deleted = false (bypass @Where)
        Order restored = (Order) entityManager
                .createNativeQuery("SELECT * FROM orders WHERE id = :id", Order.class)
                .setParameter("id", order.getId())
                .getSingleResult();

        assertThat(restored.isDeleted()).isFalse();
    }
}