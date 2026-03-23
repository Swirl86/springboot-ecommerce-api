package com.swirl.ecomengine.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    private String baseUrl;

    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void fullProductFlow() {
        // 1. Create product
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop");

        ResponseEntity<ProductResponse> createResponse =
                rest.postForEntity(baseUrl + "/products", request, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.id();

        // 2. Get product by id
        ProductResponse getResponse =
                rest.getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(getResponse.name()).isEqualTo("Laptop");
        assertThat(getResponse.price()).isEqualTo(999.99);

        // 3. Get all products
        ResponseEntity<ProductResponse[]> listResponse =
                rest.getForEntity(baseUrl + "/products", ProductResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void updateProduct_updatesExistingProduct() {
        // Create initial product
        ProductRequest createReq = new ProductRequest("Laptop", 999.99, "Powerful laptop");
        ProductResponse created = rest.postForEntity(baseUrl + "/products", createReq, ProductResponse.class).getBody();
        assertThat(created).isNotNull();

        Long id = created.id();

        // Update request
        ProductRequest updateReq = new ProductRequest("Gaming Laptop", 1499.99, "Upgraded model");

        rest.put(baseUrl + "/products/" + id, updateReq);

        // Fetch updated product
        ProductResponse updated = rest.getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(updated.name()).isEqualTo("Gaming Laptop");
        assertThat(updated.price()).isEqualTo(1499.99);
    }

    @Test
    void deleteProduct_removesProduct() {
        // Create product
        ProductRequest req = new ProductRequest("Laptop", 999.99, "Powerful laptop");
        ProductResponse created = rest.postForEntity(baseUrl + "/products", req, ProductResponse.class).getBody();
        assertThat(created).isNotNull();

        Long id = created.id();

        // Delete
        rest.delete(baseUrl + "/products/" + id);

        // Verify 404
        ResponseEntity<String> response = rest.getForEntity(baseUrl + "/products/" + id, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getProduct_returns404_whenNotFound() {
        ResponseEntity<String> response = rest.getForEntity(baseUrl + "/products/9999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
