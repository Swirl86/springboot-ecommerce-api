package com.swirl.ecomengine.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Use patterns instead of setAllowedOrigins (fixes Firefox + 304)
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        // Allowed methods
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allowed headers
        // Wildcard headers are used because Spring Security adds dynamic headers
        // (e.g., WWW-Authenticate) during auth failures. Firefox requires all such
        // headers to be allowed explicitly, so '*' ensures stable CORS behavior
        // when using CorsFilter together with Spring Security.
        config.addAllowedHeader("*");
        config.addExposedHeader("*");

        // Allow cookies / credentials
        config.setAllowCredentials(true);

        // Apply to all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}