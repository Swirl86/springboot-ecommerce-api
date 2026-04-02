package com.swirl.ecomengine.security;

public final class SecurityRules {

    private SecurityRules() {}

    // Public endpoints (no authentication required)
    public static final String[] PUBLIC = {
            "/auth/**"
    };

    // Endpoints accessible to authenticated users (GET)
    public static final String[] USER_READ = {
            "/products/**",
            "/categories/**",
            "/cart/**"
    };

    // Endpoints accessible to authenticated users (POST, PUT, DELETE)
    public static final String[] USER_WRITE = {
            "/cart/**"
    };

    // Endpoints restricted to ADMIN role
    public static final String[] ADMIN_WRITE = {
            "/products/**",
            "/categories/**"
    };
}