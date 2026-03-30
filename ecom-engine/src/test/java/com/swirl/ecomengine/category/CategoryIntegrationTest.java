package com.swirl.ecomengine.category;

import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.Role;
import com.swirl.ecomengine.user.User;
import com.swirl.ecomengine.user.UserRepository;
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
class CategoryIntegrationTest {

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

    @Test
    void createAndFetchCategory() {
        CategoryRequest request = new CategoryRequest("Electronics");

        HttpEntity<CategoryRequest> entity = new HttpEntity<>(request, authHeaders());

        ResponseEntity<CategoryResponse> createResponse =
                rest.postForEntity(baseUrl + "/categories", entity, CategoryResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CategoryResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Electronics");

        ResponseEntity<CategoryResponse[]> listResponse =
                rest.exchange(baseUrl + "/categories", HttpMethod.GET,
                        new HttpEntity<>(authHeaders()), CategoryResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }
}