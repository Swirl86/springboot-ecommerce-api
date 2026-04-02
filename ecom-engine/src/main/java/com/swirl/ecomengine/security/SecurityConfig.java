package com.swirl.ecomengine.security;

import com.swirl.ecomengine.security.handler.JsonAccessDeniedHandler;
import com.swirl.ecomengine.security.handler.JsonAuthenticationEntryPoint;
import com.swirl.ecomengine.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static com.swirl.ecomengine.security.SecurityRules.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile({"dev", "prod", "test-integration"})
public class SecurityConfig {

    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimiter authRateLimiter;
    private final CorsConfig corsConfig;

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http
                .cors(cors -> cors.configurationSource(corsConfig.corsConfigurationSource()))

                // Disable CSRF for stateless JWT
                .csrf(AbstractHttpConfigurer::disable)

                // Stateless JWT
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Global exception handling
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityRules.PUBLIC).permitAll()

                        // Swagger is denied here; in 'dev' this is overridden by SwaggerSecurityConfig
                        .requestMatchers(SWAGGER).denyAll()

                        // USER
                        .requestMatchers(HttpMethod.GET, SecurityRules.USER_READ).authenticated()
                        // USER CART
                        .requestMatchers(HttpMethod.POST, SecurityRules.USER_WRITE).authenticated()
                        .requestMatchers(HttpMethod.PUT, SecurityRules.USER_WRITE).authenticated()
                        .requestMatchers(HttpMethod.DELETE, SecurityRules.USER_WRITE).authenticated()

                        // ADMIN
                        .requestMatchers(HttpMethod.POST, SecurityRules.ADMIN_WRITE).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, SecurityRules.ADMIN_WRITE).hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, SecurityRules.ADMIN_WRITE).hasRole("ADMIN")
                )

                // Filters
                .addFilterBefore(authRateLimiter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }
}