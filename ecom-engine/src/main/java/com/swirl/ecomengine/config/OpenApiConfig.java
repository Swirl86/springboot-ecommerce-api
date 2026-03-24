package com.swirl.ecomengine.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {

    /**
     * Swagger UI: http://localhost:8080/swagger-ui.html
     */
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("EcomEngine API")
                        .description("""
                                A modular and scalable e‑commerce backend designed for product management, 
                                categories, customer accounts, shopping carts, and future checkout flows. 
                                This API provides the foundation for building a complete online store 
                                with secure data handling, clean architecture, and extensible domain models.
                                """)
                        .version("1.0.0"));
    }
}