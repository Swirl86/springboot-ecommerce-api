package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.product.dto.ProductRequest;
import com.swirl.ecomengine.product.dto.ProductResponse;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String baseUrl;
    private String adminToken;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;

        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create ADMIN user
        User admin = new User(
                null,
                "admin@example.com",
                "password", // no encoding needed for tests
                Role.ADMIN
        );
        userRepository.save(admin);

        adminToken = jwtService.generateToken(admin);
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private Long createCategory() {
        var req = new CategoryRequest("Electronics");

        HttpEntity<CategoryRequest> entity = new HttpEntity<>(req, authHeaders());

        ResponseEntity<CategoryResponse> res =
                rest.postForEntity(baseUrl + "/categories", entity, CategoryResponse.class);

        assertThat(res.getBody()).isNotNull();
        return res.getBody().id();
    }

    @Test
    void fullProductFlow() {
        Long categoryId = createCategory();

        ProductRequest request =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        HttpEntity<ProductRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<ProductResponse> createResponse =
                rest.postForEntity(baseUrl + "/products", entity, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ProductResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.id();

        ResponseEntity<ProductResponse> getResponse =
                rest.exchange(baseUrl + "/products/" + id, HttpMethod.GET,
                        new HttpEntity<>(authHeaders()), ProductResponse.class);

        Assertions.assertNotNull(getResponse.getBody());
        assertThat(getResponse.getBody().name()).isEqualTo("Laptop");
        assertThat(getResponse.getBody().categoryId()).isEqualTo(categoryId);

        ResponseEntity<ProductResponse[]> listResponse =
                rest.exchange(baseUrl + "/products", HttpMethod.GET,
                        new HttpEntity<>(authHeaders()), ProductResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void updateProduct_updatesExistingProduct() {
        Long categoryId = createCategory();

        ProductRequest createReq =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        HttpEntity<ProductRequest> createEntity = new HttpEntity<>(createReq, authHeaders());

        ProductResponse created =
                rest.postForEntity(baseUrl + "/products", createEntity, ProductResponse.class)
                        .getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        ProductRequest updateReq =
                new ProductRequest("Gaming Laptop", 1499.99, "Upgraded model", categoryId);

        rest.exchange(baseUrl + "/products/" + id, HttpMethod.PUT,
                new HttpEntity<>(updateReq, authHeaders()), Void.class);

        ProductResponse updated =
                rest.exchange(baseUrl + "/products/" + id, HttpMethod.GET,
                                new HttpEntity<>(authHeaders()), ProductResponse.class)
                        .getBody();

        Assertions.assertNotNull(updated);
        assertThat(updated.name()).isEqualTo("Gaming Laptop");
        assertThat(updated.price()).isEqualTo(1499.99);
    }

    @Test
    void deleteProduct_removesProduct() {
        Long categoryId = createCategory();

        ProductRequest req =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ProductResponse created =
                rest.postForEntity(baseUrl + "/products",
                                new HttpEntity<>(req, authHeaders()),
                                ProductResponse.class)
                        .getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        rest.exchange(baseUrl + "/products/" + id, HttpMethod.DELETE,
                new HttpEntity<>(authHeaders()), Void.class);

        ResponseEntity<String> response =
                rest.exchange(baseUrl + "/products/" + id, HttpMethod.GET,
                        new HttpEntity<>(authHeaders()), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
