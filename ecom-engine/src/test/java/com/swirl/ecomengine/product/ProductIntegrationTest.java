package com.swirl.ecomengine.product;

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

    @Test
    void fullProductFlow() {
        // 1. Create product
        ProductRequest request = new ProductRequest("Laptop", 999.99, "Powerful laptop");

        ResponseEntity<ProductResponse> createResponse =
                rest.postForEntity("http://localhost:" + port + "/products", request, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.id();

        // 2. Get product by id
        ProductResponse getResponse =
                rest.getForObject("http://localhost:" + port + "/products/" + id, ProductResponse.class);

        assertThat(getResponse.name()).isEqualTo("Laptop");
        assertThat(getResponse.price()).isEqualTo(999.99);

        // 3. Get all products
        ResponseEntity<ProductResponse[]> listResponse =
                rest.getForEntity("http://localhost:" + port + "/products", ProductResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }
}
