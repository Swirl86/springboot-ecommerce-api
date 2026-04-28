package com.swirl.ecomengine.security.util;

public final class SecurityRules {

    private SecurityRules() {}

    // Public auth endpoints (login, register)
    public static final String[] AUTH = {
            "/auth/**"
    };

    // Public GET endpoints
    public static final String[] PUBLIC = {
            "/products/**",
            "/categories/**",
            "/health",
            "/health/details"
    };

    // Swagger (open only in dev)
    public static final String[] SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml"
    };

    // Endpoints accessible to authenticated users (GET)
    public static final String[] USER_READ = {
            "/cart/**",
            "/orders",          // list orders
            "/orders/*",         // get order by id
            "/orders/*/history/**",
            "/users/me",
            "/users/me/address",
            "/users/me/full-profile",
            "/wishlist",
            "/wishlist/**"
    };

    // Endpoints accessible to authenticated users (POST, PUT, DELETE)
    public static final String[] USER_WRITE = {
            "/cart/**",
            "/orders/checkout",
            "/users/me",
            "/users/me/address",
            "/wishlist/**"
    };

    // Endpoints restricted to ADMIN role
    public static final String[] ADMIN_WRITE = {
            "/products/**",
            "/categories/**",
            "/orders/**",
            "/orders/*/restore",
            "/admin/**"
    };
}