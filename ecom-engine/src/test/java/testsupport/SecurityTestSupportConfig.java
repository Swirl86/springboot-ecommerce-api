package testsupport;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swirl.ecomengine.security.config.CorsConfig;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.security.util.AuthRateLimiter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Lightweight security support configuration for tests.
 * <pre>
 * This configuration is intended for:
 *  - @SpringBootTest integration tests that rely on the real SecurityConfig
 *  - Tests that need a stable JwtService with a static secret
 *  - Tests that require SecurityConfig dependencies (AuthRateLimiter, CorsConfig)
 * <pre>
 * What this configuration DOES:
 *  - Provides a test‑friendly JwtService (static secret, predictable tokens)
 *  - Provides AuthRateLimiter and CorsConfig so SecurityConfig can initialize
 *  - Provides Jackson configuration so LocalDateTime serializes correctly
 * <pre>
 * What this configuration DOES NOT do:
 *  - It does NOT replace the real SecurityFilterChain
 *  - It does NOT mock UserRepository
 *  - It does NOT override JwtAuthenticationFilter
 * <pre>
 * This ensures that integration tests run against the real security setup,
 * while still having deterministic JWT behavior and required dependencies.
 */
@TestConfiguration
public class SecurityTestSupportConfig {

    /**
     * Test‑friendly JwtService with a static secret.
     * Ensures predictable JWT generation and validation in tests.
     */
    @Bean
    public JwtService testJwtService() {
        return new JwtService(
                "test-secret-test-secret-test-secret-123456",
                3600000
        );
    }

    /**
     * Required by SecurityConfig.
     * The limiter is active but harmless in test environments.
     */
    @Bean
    public AuthRateLimiter testAuthRateLimiter() {
        return new AuthRateLimiter();
    }

    /**
     * Required by SecurityConfig.
     * Provides minimal CORS configuration for test contexts.
     */
    @Bean
    public CorsConfig testCorsConfig() {
        return new CorsConfig();
    }

    /**
     * Ensures consistent JSON serialization in tests,
     * especially for LocalDateTime in error responses.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer testJacksonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}