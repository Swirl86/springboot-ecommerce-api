package com.swirl.ecomengine.security;

public final class SecurityRules {

    private SecurityRules() {}

    public static final String[] PUBLIC = {
            "/auth/**"
    };

    public static final String[] USER_READ = {
            "/products/**",
            "/categories/**"
    };

    public static final String[] ADMIN_WRITE = {
            "/products/**",
            "/categories/**"
    };
}
