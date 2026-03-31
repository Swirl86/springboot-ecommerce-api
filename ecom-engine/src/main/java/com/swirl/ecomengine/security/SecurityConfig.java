package com.swirl.ecomengine.security;

import com.swirl.ecomengine.security.jwt.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

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
                        .authenticationEntryPoint((req, res, ex2) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((req, res, ex2) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN))
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(SecurityRules.PUBLIC).permitAll()
                        .requestMatchers(HttpMethod.GET, SecurityRules.USER_READ).authenticated()
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