package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.CategoryRepository;
import com.swirl.ecomengine.category.CategoryRequest;
import com.swirl.ecomengine.category.CategoryResponse;
import com.swirl.ecomengine.security.TestSecurityConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;

        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Long createCategory() {
        var req = new CategoryRequest("Electronics");

        var res = rest.withBasicAuth("admin", "admin")
                .postForEntity(baseUrl + "/categories", req, CategoryResponse.class);

        Assertions.assertNotNull(res.getBody());
        return res.getBody().id();
    }

    @Test
    void fullProductFlow() {
        Long categoryId = createCategory();

        ProductRequest request =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ResponseEntity<ProductResponse> createResponse =
                rest.withBasicAuth("admin", "admin")
                        .postForEntity(baseUrl + "/products", request, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ProductResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.id();

        ProductResponse getResponse =
                rest.withBasicAuth("admin", "admin")
                        .getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(getResponse.name()).isEqualTo("Laptop");
        assertThat(getResponse.categoryId()).isEqualTo(categoryId);

        ResponseEntity<ProductResponse[]> listResponse =
                rest.withBasicAuth("admin", "admin")
                        .getForEntity(baseUrl + "/products", ProductResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void updateProduct_updatesExistingProduct() {
        Long categoryId = createCategory();

        ProductRequest createReq =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ProductResponse created =
                rest.withBasicAuth("admin", "admin")
                        .postForEntity(baseUrl + "/products", createReq, ProductResponse.class)
                        .getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        ProductRequest updateReq =
                new ProductRequest("Gaming Laptop", 1499.99, "Upgraded model", categoryId);

        rest.withBasicAuth("admin", "admin")
                .put(baseUrl + "/products/" + id, updateReq);

        ProductResponse updated =
                rest.withBasicAuth("admin", "admin")
                        .getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(updated.name()).isEqualTo("Gaming Laptop");
        assertThat(updated.price()).isEqualTo(1499.99);
    }

    @Test
    void deleteProduct_removesProduct() {
        Long categoryId = createCategory();

        ProductRequest req =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ProductResponse created =
                rest.withBasicAuth("admin", "admin")
                        .postForEntity(baseUrl + "/products", req, ProductResponse.class)
                        .getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        rest.withBasicAuth("admin", "admin")
                .delete(baseUrl + "/products/" + id);

        ResponseEntity<String> response =
                rest.withBasicAuth("admin", "admin")
                        .getForEntity(baseUrl + "/products/" + id, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
