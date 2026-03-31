package testsupport;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Minimal security configuration used for controller tests
 * where authentication and authorization should be disabled.
 * <pre>
 * This setup is ideal for:
 *  - @WebMvcTest without JWT filters
 *  - Testing validation, JSON mapping, and controller logic
 *  - Ensuring security does not interfere with unit-level tests
 * <pre>
 * All requests are permitted and no filters are applied.
 */
@TestConfiguration
public class SecurityTestConfigMinimal {

    @Bean
    public SecurityFilterChain minimalSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
