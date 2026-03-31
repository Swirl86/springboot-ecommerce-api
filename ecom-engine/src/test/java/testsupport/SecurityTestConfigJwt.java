package testsupport;

import com.swirl.ecomengine.security.AuthRateLimiter;
import com.swirl.ecomengine.security.CorsConfig;
import com.swirl.ecomengine.security.jwt.JwtAuthenticationFilter;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.UserRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityTestConfig {

    /**
     * A lightweight JwtService used only for test environments.
     * Uses a static test secret and a short-lived expiration.
     * Ensures predictable and isolated JWT behavior in tests.
     */
    @Bean
    public JwtService testJwtService() {
        return new JwtService(
                "test-secret-test-secret-test-secret-123456",
                3600000 // 1 hour
        );
    }

    /**
     * Mocked UserRepository to avoid database access during controller tests.
     * JwtAuthenticationFilter depends on this repository for user lookups.
     */
    @Bean
    public UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    /**
     * JwtAuthenticationFilter wired with test JwtService + mocked UserRepository.
     * Only used when addFilters=true in WebMvcTest or in full integration tests.
     */
    @Bean
    public JwtAuthenticationFilter testJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(testJwtService(), mockUserRepository());
    }

    /**
     * AuthRateLimiter used in tests to satisfy SecurityConfig dependencies.
     * The limiter is active but harmless in test environments.
     */
    @Bean
    public AuthRateLimiter testAuthRateLimiter() {
        return new AuthRateLimiter();
    }

    /**
     * CorsConfig used in tests to satisfy SecurityConfig dependencies.
     * Provides a minimal CORS configuration for test contexts.
     */
    @Bean
    public CorsConfig testCorsConfig() {
        return new CorsConfig();
    }
}