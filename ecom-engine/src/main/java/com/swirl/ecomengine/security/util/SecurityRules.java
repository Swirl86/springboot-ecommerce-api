package com.swirl.ecomengine.security.util;

public final class SecurityRules {

    private SecurityRules() {}

    // Public endpoints (no authentication required)
    public static final String[] PUBLIC = {
            "/auth/**"
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
            "/products/**",
            "/categories/**",
            "/cart/**",
            "/orders/**"
    };

    // Endpoints accessible to authenticated users (POST, PUT, DELETE)
    public static final String[] USER_WRITE = {
            "/cart/**",
            "/orders/checkout"
    };

    // Endpoints restricted to ADMIN role
    public static final String[] ADMIN_WRITE = {
            "/products/**",
            "/categories/**"
    };
}