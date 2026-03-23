package com.swirl.ecomengine.product;

import com.swirl.ecomengine.category.CategoryRequest;
import com.swirl.ecomengine.category.CategoryResponse;
import org.junit.jupiter.api.Assertions;
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

    private Long createCategory() {
        var req = new CategoryRequest("Electronics");
        var res = rest.postForEntity(baseUrl + "/categories", req, CategoryResponse.class);
        Assertions.assertNotNull(res.getBody());
        return res.getBody().id();
    }

    @Test
    void fullProductFlow() {
        Long categoryId = createCategory();

        ProductRequest request =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ResponseEntity<ProductResponse> createResponse =
                rest.postForEntity(baseUrl + "/products", request, ProductResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        ProductResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        Long id = created.id();

        ProductResponse getResponse =
                rest.getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(getResponse.name()).isEqualTo("Laptop");
        assertThat(getResponse.categoryId()).isEqualTo(categoryId);

        ResponseEntity<ProductResponse[]> listResponse =
                rest.getForEntity(baseUrl + "/products", ProductResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }

    @Test
    void updateProduct_updatesExistingProduct() {
        Long categoryId = createCategory();

        ProductRequest createReq =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ProductResponse created =
                rest.postForEntity(baseUrl + "/products", createReq, ProductResponse.class).getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        ProductRequest updateReq =
                new ProductRequest("Gaming Laptop", 1499.99, "Upgraded model", categoryId);

        rest.put(baseUrl + "/products/" + id, updateReq);

        ProductResponse updated =
                rest.getForObject(baseUrl + "/products/" + id, ProductResponse.class);

        assertThat(updated.name()).isEqualTo("Gaming Laptop");
        assertThat(updated.price()).isEqualTo(1499.99);
    }

    @Test
    void deleteProduct_removesProduct() {
        Long categoryId = createCategory();

        ProductRequest req =
                new ProductRequest("Laptop", 999.99, "Powerful laptop", categoryId);

        ProductResponse created =
                rest.postForEntity(baseUrl + "/products", req, ProductResponse.class).getBody();

        Assertions.assertNotNull(created);
        Long id = created.id();

        rest.delete(baseUrl + "/products/" + id);

        ResponseEntity<String> response =
                rest.getForEntity(baseUrl + "/products/" + id, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
