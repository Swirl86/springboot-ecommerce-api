package testsupport;

import com.swirl.ecomengine.security.jwt.JwtAuthenticationFilter;
import com.swirl.ecomengine.security.jwt.JwtService;
import com.swirl.ecomengine.user.UserRepository;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SecurityTestConfig {

    /**
     * A lightweight JwtService used only for WebMvcTest environments.
     * Uses a static test secret and a short-lived expiration.
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
     */
    @Bean
    public UserRepository mockUserRepository() {
        return Mockito.mock(UserRepository.class);
    }

    /**
     * JwtAuthenticationFilter wired with test JwtService + mocked UserRepository.
     * Only used when addFilters=true in WebMvcTest.
     */
    @Bean
    public JwtAuthenticationFilter testJwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(testJwtService(), mockUserRepository());
    }
}