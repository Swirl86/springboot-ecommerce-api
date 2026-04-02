package com.swirl.ecomengine.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static com.swirl.ecomengine.security.SecurityRules.SWAGGER;

/**
 * Security configuration that applies only in the 'dev' profile.
 * <pre>
 * This filter chain is registered with the highest precedence (Order 0)
 * and overrides the main SecurityConfig for Swagger-related endpoints.
 * <pre>
 * Its purpose is to make Swagger UI and OpenAPI documentation accessible
 * during development, while keeping them fully protected in all other
 * environments (prod etc.).
 */
@Configuration
@Profile("dev")
public class SwaggerSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain swaggerSecurity(HttpSecurity http) throws Exception {
        http
                .securityMatcher(SWAGGER)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }
}