package com.swirl.ecomengine.category;

import com.swirl.ecomengine.category.dto.CategoryRequest;
import com.swirl.ecomengine.category.dto.CategoryResponse;
import com.swirl.ecomengine.product.ProductRepository;
import com.swirl.ecomengine.security.TestSecurityConfig;
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
class CategoryIntegrationTest {

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

    @Test
    void createAndFetchCategory() {
        CategoryRequest request = new CategoryRequest("Electronics");

        ResponseEntity<CategoryResponse> createResponse =
                rest.withBasicAuth("admin", "admin")
                        .postForEntity(baseUrl + "/categories", request, CategoryResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        CategoryResponse created = createResponse.getBody();
        assertThat(created).isNotNull();
        assertThat(created.name()).isEqualTo("Electronics");

        ResponseEntity<CategoryResponse[]> listResponse =
                rest.withBasicAuth("admin", "admin")
                        .getForEntity(baseUrl + "/categories", CategoryResponse[].class);

        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResponse.getBody()).isNotEmpty();
    }
}