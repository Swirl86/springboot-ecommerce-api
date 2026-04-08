package testsupport;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.swirl.ecomengine.security.config.CorsConfig;
import com.swirl.ecomengine.security.jwt.JwtAuthenticationFilter;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.security.util.AuthRateLimiter;
import com.swirl.ecomengine.user.UserRepository;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test configuration used for full @SpringBootTest integration tests.
 * <pre>
 * This configuration provides:
 *  - A test-friendly JwtService (static secret, no external config needed)
 *  - A JwtAuthenticationFilter wired with test JwtService + mocked UserRepository
 *  - AuthRateLimiter + CorsConfig to satisfy SecurityConfig dependencies
 *  - Provide a minimal SecurityFilterChain (CSRF disabled, /auth/** open)
 *  - A fully configured ObjectMapper with JavaTimeModule so LocalDateTime
 *    can be serialized in ErrorResponse and security handlers
 * <pre>
 * This ensures that the real SecurityConfig can initialize correctly
 * inside @SpringBootTest environments using the "test-integration" profile.
 */
@TestConfiguration
public class IntegrationTestConfig {

    /**
     * Lightweight JwtService with static test secret.
     * Avoids loading secrets from application properties.
     */
    @Bean
    public JwtService testJwtService() {
        return new JwtService(
                "test-secret-test-secret-test-secret-123456",
                3600000
        );
    }

    /**
     * Mocked UserRepository used by JwtAuthenticationFilter.
     * Real DB access happens through repositories injected in tests.
     */
    @Bean(name = "jwtUserRepository")
    public UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    /**
     * JwtAuthenticationFilter wired with test JwtService + mocked UserRepository.
     * This filter is required by SecurityConfig to build the real filter chain.
     */
    @Bean
    public JwtAuthenticationFilter testJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(testJwtService(), mockUserRepository());
    }

    /** Minimal security chain for integration tests (CSRF off, /auth/** open). */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    /**
     * AuthRateLimiter used in tests to satisfy SecurityConfig dependencies.
     */
    @Bean
    public AuthRateLimiter testAuthRateLimiter() {
        return new AuthRateLimiter();
    }

    /**
     * CORS configuration for test environments.
     */
    @Bean
    public CorsConfig testCorsConfig() {
        return new CorsConfig();
    }

    /**
     * Adds JavaTimeModule so LocalDateTime serializes correctly in all test ObjectMappers.
     * Ensures MockMvc and security handlers use ISO‑8601 instead of timestamps.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer testJacksonCustomizer() {
        return builder -> {
            builder.modules(new JavaTimeModule());
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}